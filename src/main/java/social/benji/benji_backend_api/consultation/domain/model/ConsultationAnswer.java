package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.UrgencyLevel;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Value object representing the expert's final structured answer.
 * Immutable after submission (except through explicit correction/versioning).
 */
@Entity
@Table(name = "consultation_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationAnswer {

    @Id
    private UUID id;
    private UUID expertId;
    private String assessment;
    private String recommendedActions;
    private String warningSigns;
    private boolean inPersonVisitRecommended;
    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgency;
    private Instant submittedAt;
}
