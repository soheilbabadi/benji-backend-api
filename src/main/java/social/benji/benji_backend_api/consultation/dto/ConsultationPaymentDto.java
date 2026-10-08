package social.benji.benji_backend_api.consultation.dto;

import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationPaymentDto {

    private String id;
    private String consultationId;
    private String providerPaymentId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String idempotencyKey;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant paidAt;
}
