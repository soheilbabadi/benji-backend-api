package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import social.benji.benji_backend_api.consultation.domain.valueobject.UrgencyLevel;

import java.time.Instant;

/**
 * Value object representing the expert's final structured answer.
 * Immutable after submission (except through explicit correction/versioning).
 */
@Document(collection = "consultation_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationAnswer {

    @Id
    private String id;
    private String expertId;
    private String assessment;
    private String recommendedActions;
    private String warningSigns;
    private boolean inPersonVisitRecommended;
    private UrgencyLevel urgency;
    private Instant submittedAt;
}
