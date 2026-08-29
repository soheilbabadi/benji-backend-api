package social.benji.benji_backend_api.consultation.domain.model;

import lombok.Getter;
import social.benji.benji_backend_api.consultation.domain.valueobject.PaymentStatus;
import social.benji.benji_backend_api.consultation.domain.valueobject.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a payment record for a consultation.
 * Payment status transitions are controlled and validated.
 */
@Getter
public class ConsultationPayment {

    private final UUID id;
    private final UUID consultationId;
    private String providerPaymentId; // ID from Stripe/PayPal/etc.
    private PaymentStatus status;
    private final Money amount;
    private String idempotencyKey; // For idempotent callback processing
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant paidAt;

    protected ConsultationPayment() {
        // For JPA
        this.id = null;
        this.consultationId = null;
        this.status = null;
        this.amount = null;
        this.createdAt = null;
        this.updatedAt = null;
        this.paidAt = null;
    }

    public ConsultationPayment(UUID consultationId, Money amount, String idempotencyKey) {
        this.id = UUID.randomUUID();
        this.consultationId = Objects.requireNonNull(consultationId, "Consultation ID cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Marks payment as successful.
     * Idempotent operation - safe to call multiple times with same providerPaymentId.
     */
    public void markSuccess(String providerPaymentId) {
        if (this.status == PaymentStatus.SUCCESS) {
            // Already marked as success, idempotent
            return;
        }
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Cannot mark payment as success from status: " + this.status);
        }
        this.providerPaymentId = Objects.requireNonNull(providerPaymentId, "Provider payment ID cannot be null");
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Marks payment as failed.
     */
    public void markFailed() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Cannot mark payment as failed from status: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    /**
     * Marks payment as refunded.
     */
    public void markRefunded() {
        if (this.status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Can only refund successful payments");
        }
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = Instant.now();
    }

    /**
     * Marks payment as cancelled.
     */
    public void markCancelled() {
        if (this.status != PaymentStatus.PENDING && this.status != PaymentStatus.FAILED) {
            throw new IllegalStateException("Can only cancel pending or failed payments");
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public boolean isSuccessful() {
        return this.status == PaymentStatus.SUCCESS;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }
}
