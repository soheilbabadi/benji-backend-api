package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import social.benji.benji_backend_api.consultation.domain.valueobject.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a payment record for a consultation.
 * Payment status transitions are controlled and validated.
 */
@Document(collection = "consultation_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationPayment {

    @Id
    private String id;
    private String consultationId;
    private String providerPaymentId; // ID from Stripe/PayPal/etc.
    private PaymentStatus status;

    private BigDecimal amount;
    private String idempotencyKey; // For idempotent callback processing
    private Instant createdAt;
    private Instant updatedAt;
    private Instant paidAt;
}
