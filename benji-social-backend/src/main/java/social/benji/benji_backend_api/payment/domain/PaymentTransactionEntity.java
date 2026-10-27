package social.benji.benji_backend_api.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import social.benji.benji_backend_api.exception.BenjiCustomException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A single payment attempt for a business operation of another Benji module.
 *
 * <p>Design rules encoded here (all approved):</p>
 * <ul>
 *   <li><b>Cross-module reference is generic:</b> {@code serviceType} + {@code serviceReferenceId}
 *       only — no FK to Lost &amp; Found / Consultation tables, no knowledge of their lifecycles.</li>
 *   <li><b>Money snapshot:</b> {@code amount}/{@code currency} are copied from the tariff at
 *       creation and never change when tariffs change. Integer IRR Rials, NUMERIC(19,0), BigDecimal.</li>
 *   <li><b>Attempts = separate transactions</b> (decision C); DB partial unique indexes guarantee
 *       ≤1 in-flight and ≤1 SUCCESS row per business operation.</li>
 *   <li><b>Status changes go exclusively through {@link #transitionTo}</b>, which enforces the
 *       state machine ({@link PaymentStatus#canTransitionTo}); a verified SUCCESS can never be
 *       demoted by a duplicate/delayed callback.</li>
 *   <li><b>Optimistic locking</b> via {@link #version}; the verify path additionally takes a
 *       pessimistic row lock (SELECT … FOR UPDATE) so concurrent callbacks serialize.</li>
 * </ul>
 */
@Entity
@Table(name = "payment_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Client-facing identifier used in REST paths; the internal PK is never exposed. */
    @Column(name = "public_token", nullable = false, updatable = false, unique = true)
    @Builder.Default
    private UUID publicToken = UUID.randomUUID();

    /**
     * Owner. Stored as plain UUID: no FK into the user module (modular-monolith boundary).
     * String because users are identified by VARCHAR ids in this schema; payment never joins
     * to the user table, it only compares ownership values.
     */
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 64)
    private ServiceType serviceType;

    /** ID of the business object being paid for (ad id, consultation id…). No FK by design. */
    @Column(name = "service_reference_id", nullable = false)
    private UUID serviceReferenceId;

    // ---- Tariff snapshot (immutable after creation) ----------------------------------------

    @Column(name = "tariff_id", nullable = false)
    private UUID tariffId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = CurrencyCodes.IRR;

    // ---- Gateway ---------------------------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway", nullable = false, length = 32)
    @Builder.Default
    private PaymentGateway gateway = PaymentGateway.ZARINPAL;

    /** ZarinPal authority (UUID string). Unique across rows → gateway-side idempotency anchor. */
    @Column(name = "gateway_authority", length = 100)
    private String gatewayAuthority;

    /** ZarinPal ref_id returned on successful verification. */
    @Column(name = "gateway_reference_id", length = 100)
    private String gatewayReferenceId;

    /** Correlation id of the gateway request call, for support/tracing. */
    @Column(name = "gateway_request_id", length = 100)
    private String gatewayRequestId;

    // ---- Status & lifecycle ------------------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "initiated_at")
    private Instant initiatedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    // ---- Diagnostics (sanitized; NEVER merchant secrets or card data) -------------------------

    @Column(name = "failure_code", length = 64)
    private String failureCode;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "diagnostic_info")
    private String diagnosticInfo;

    @Column(name = "verify_attempts", nullable = false)
    @Builder.Default
    private int verifyAttempts = 0;

    @Column(name = "last_verify_error", length = 512)
    private String lastVerifyError;

    // ---- Refund (MVP: record-only, decision G) -------------------------------------------------

    @Column(name = "refund_reference_id", length = 100)
    private String refundReferenceId;

    @Column(name = "refunded_by_user_id", length = 64)
    private String refundedByUserId;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    // ---- Factory -------------------------------------------------------------------------------

    /**
     * Creates a new PENDING attempt pricing {@code serviceType/serviceReferenceId} from the given
     * tariff snapshot. The caller must already have validated the active tariff and the business
     * reference; amounts are never accepted from clients.
     */
    public static PaymentTransactionEntity createPending(String userId,
                                                         ServiceType serviceType,
                                                         UUID serviceReferenceId,
                                                         TariffEntity tariff) {
        if (userId == null || serviceType == null || serviceReferenceId == null || tariff == null) {
            throw new BenjiCustomException.ValidationException("Cannot create a payment with missing fields");
        }
        Instant now = Instant.now();
        return PaymentTransactionEntity.builder()
                .userId(userId)
                .serviceType(serviceType)
                .serviceReferenceId(serviceReferenceId)
                .tariffId(tariff.getId())
                .amount(tariff.getAmount())
                .currency(tariff.getCurrency())
                .gateway(PaymentGateway.ZARINPAL)
                .status(PaymentStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ---- State-machine guarded transitions -------------------------------------------------------

    /**
     * Moves this transaction to {@code next}, enforcing {@link PaymentStatus#canTransitionTo}.
     * Illegal moves (e.g. SUCCESS→FAILED from a delayed callback) throw a conflict.
     */
    public void transitionTo(PaymentStatus next) {
        if (status == null) {
            throw new BenjiCustomException.ApplicationServerException("Transaction has no status");
        }
        if (!status.canTransitionTo(next)) {
            throw new BenjiCustomException.DuplicateEntityException(
                    "Illegal payment status transition " + status + " -> " + next
                            + " for transaction " + publicToken);
        }
        this.status = next;
        touch();
    }

    /** Records that a gateway redirect was issued for this attempt. */
    public void markInitiated(String authority, String requestId) {
        this.gatewayAuthority = authority;
        this.gatewayRequestId = requestId;
        this.initiatedAt = Instant.now();
        touch();
    }

    /** Settles a server-side verified success (result code 100, or 101/102 already-verified). */
    public void markVerifiedSuccess(String gatewayRefId) {
        transitionTo(PaymentStatus.SUCCESS);
        this.gatewayReferenceId = gatewayRefId;
        this.verifiedAt = Instant.now();
        this.paidAt = this.verifiedAt;
        this.failureCode = null;
        this.failureReason = null;
    }

    /** Settles a definitive gateway failure. Never call this for timeouts/uncertain outcomes. */
    public void markFailed(String code, String sanitizedReason) {
        transitionTo(PaymentStatus.FAILED);
        this.failureCode = code;
        this.failureReason = sanitizedReason;
        this.verifiedAt = Instant.now();
    }

    /** Records an uncertain outcome (timeout / ambiguous response): retry later, never terminal. */
    public void markUnknown(String sanitizedError) {
        transitionTo(PaymentStatus.UNKNOWN);
        this.verifyAttempts++;
        this.lastVerifyError = sanitize(sanitizedError);
    }

    /** Marks the start of a verification pass (guarded by SELECT … FOR UPDATE elsewhere). */
    public void markVerifying() {
        transitionTo(PaymentStatus.VERIFYING);
        this.verifyAttempts++;
    }

    /** User abandoned checkout before verification. */
    public void markCancelled(String reason) {
        transitionTo(PaymentStatus.CANCELLED);
        this.failureCode = "USER_CANCELLED";
        this.failureReason = reason;
    }

    /** MVP refunds are record-only (decision G): admin confirms the refund happened out-of-band. */
    public void markRefundedRecordOnly(String gatewayRefundReference, String adminUserId) {
        transitionTo(PaymentStatus.REFUNDED);
        this.refundReferenceId = gatewayRefundReference;
        this.refundedByUserId = adminUserId;
        this.refundedAt = Instant.now();
    }

    /** Keeps diagnostics bounded and free of anything resembling credentials. */
    private static String sanitize(String raw) {
        if (raw == null) return null;
        String s = raw.length() > 480 ? raw.substring(0, 480) + "…" : raw;
        return s.replaceAll("(?i)(merchant[_-]?id|authority|token|secret)[=:\\s]+[^&,\\s]+", "$1=[REDACTED]");
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
