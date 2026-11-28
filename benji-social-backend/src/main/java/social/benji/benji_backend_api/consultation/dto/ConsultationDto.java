package social.benji.benji_backend_api.consultation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.ConsultationCategory;
import social.benji.benji_backend_api.consultation.domain.valueobject.ConsultationStatus;
import social.benji.benji_backend_api.consultation.domain.valueobject.PetDataType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String ownerId;
    private String petId;
    private ConsultationCategory category;
    private String subject;
    private String question;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ConsultationStatus status;
    private BigDecimal price;
    private boolean emergencyDisclaimerAccepted;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String assignedExpertId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant updatedAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant closedAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant answeredAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long version;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<PetDataType> sharedPetDataTypes;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> attachmentIds;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ConsultationAnswerDto answer;
}
