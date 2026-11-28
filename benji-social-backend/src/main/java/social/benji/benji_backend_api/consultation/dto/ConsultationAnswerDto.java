package social.benji.benji_backend_api.consultation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.UrgencyLevel;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationAnswerDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    private String consultationId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String expertId;
    private String assessment;
    private String recommendedActions;
    private String warningSigns;
    private boolean inPersonVisitRecommended;
    private UrgencyLevel urgency;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant submittedAt;
}
