package social.benji.benji_backend_api.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Maps an idempotency key to the transaction it produced, so two concurrent "pay for this
 * operation" requests from the same user converge on ONE attempt instead of racing against the
 * partial unique indexes. The primary key IS the dedup guard (unique insert; loser reads winner).
 */
@Entity
@Table(name = "payment_idempotency_key")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentIdempotencyKeyEntity {

    @Id
    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 64)
    private ServiceType serviceType;

    @Column(name = "service_reference_id", nullable = false)
    private UUID serviceReferenceId;

    @Column(name = "payment_transaction_id", nullable = false)
    private UUID paymentTransactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /** Expired keys are pruned by the reconciliation job; they never block a fresh attempt. */
    @Column(name = "expires_at")
    private Instant expiresAt;
}
