package social.benji.benji_backend_api.consultation.dto;

import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.UrgencyLevel;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationAnswerDto {

    private String id;
    private String expertId;
    private String assessment;
    private String recommendedActions;
    private String warningSigns;
    private boolean inPersonVisitRecommended;
    private UrgencyLevel urgency;
    private Instant submittedAt;
}
