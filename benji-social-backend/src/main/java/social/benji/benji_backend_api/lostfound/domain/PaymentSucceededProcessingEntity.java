package social.benji.benji_backend_api.lostfound.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lost_found_payment_succeeded_processing")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentSucceededProcessingEntity {
    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "advertisement_public_id", nullable = false, updatable = false)
    private UUID advertisementPublicId;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public void markProcessed(Instant now) {
        processedAt = now;
    }
}
