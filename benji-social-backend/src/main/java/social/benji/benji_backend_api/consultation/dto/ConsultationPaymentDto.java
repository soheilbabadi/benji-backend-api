package social.benji.benji_backend_api.consultation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    private String consultationId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String providerPaymentId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private PaymentStatus status;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal amount;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String idempotencyKey;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant updatedAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant paidAt;
}
