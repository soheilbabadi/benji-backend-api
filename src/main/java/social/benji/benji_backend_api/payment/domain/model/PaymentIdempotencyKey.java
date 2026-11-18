package social.benji.benji_backend_api.payment.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_idempotency_key")
@Getter
@NoArgsConstructor
public class PaymentIdempotencyKey {
    @Id
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, columnDefinition = "paid_service_type")
    private PaidServiceType serviceType;

    @Column(name = "service_reference_id", nullable = false)
    private UUID serviceReferenceId;

    @Column(name = "payment_transaction_id", nullable = false)
    private UUID paymentTransactionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "expires_at")
    private Instant expiresAt;

    public PaymentIdempotencyKey(String idempotencyKey, UUID userId, PaidServiceType serviceType,
                                 UUID serviceReferenceId, UUID paymentTransactionId) {
        this.idempotencyKey = idempotencyKey;
        this.userId = userId;
        this.serviceType = serviceType;
        this.serviceReferenceId = serviceReferenceId;
        this.paymentTransactionId = paymentTransactionId;
        this.expiresAt = Instant.now().plusSeconds(24 * 60 * 60);
    }
}
