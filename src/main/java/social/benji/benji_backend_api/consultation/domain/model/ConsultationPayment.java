package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.PaymentStatus;
import social.benji.benji_backend_api.consultation.domain.valueobject.Money;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a payment record for a consultation.
 * Payment status transitions are controlled and validated.
 */
@Entity
@Table(name = "consultation_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationPayment {

    @Id
    private UUID id;
    private UUID consultationId;
    private String providerPaymentId; // ID from Stripe/PayPal/etc.
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Embedded
    private Money amount;
    private String idempotencyKey; // For idempotent callback processing
    private Instant createdAt;
    private Instant updatedAt;
    private Instant paidAt;
}
