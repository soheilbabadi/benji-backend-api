package social.benji.benji_backend_api.consultation.dto;

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

    private String id;
    private String ownerId;
    private String petId;
    private ConsultationCategory category;
    private String subject;
    private String question;
    private ConsultationStatus status;
    private BigDecimal price;
    private boolean emergencyDisclaimerAccepted;
    private String assignedExpertId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;
    private Instant answeredAt;
    private long version;
    private Set<PetDataType> sharedPetDataTypes;
    private List<String> attachmentIds;
    private ConsultationAnswerDto answer;
}
