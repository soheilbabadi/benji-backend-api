package social.benji.benji_backend_api.consultation.domain.model;

import lombok.Getter;
import social.benji.benji_backend_api.consultation.domain.valueobject.UrgencyLevel;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing the expert's final structured answer.
 * Immutable after submission (except through explicit correction/versioning).
 */
@Getter
public class ConsultationAnswer {

    private final UUID id;
    private final UUID expertId;
    private final String assessment;
    private final String recommendedActions;
    private final String warningSigns;
    private final boolean inPersonVisitRecommended;
    private final UrgencyLevel urgency;
    private final Instant submittedAt;

    protected ConsultationAnswer() {
        // For JPA
        this.id = null;
        this.expertId = null;
        this.assessment = null;
        this.recommendedActions = null;
        this.warningSigns = null;
        this.inPersonVisitRecommended = false;
        this.urgency = null;
        this.submittedAt = null;
    }

    public ConsultationAnswer(UUID expertId, String assessment, String recommendedActions,
                             String warningSigns, boolean inPersonVisitRecommended, UrgencyLevel urgency) {
        this.id = UUID.randomUUID();
        this.expertId = Objects.requireNonNull(expertId, "Expert ID cannot be null");
        this.assessment = Objects.requireNonNull(assessment, "Assessment cannot be null");
        if (assessment.isBlank()) {
            throw new IllegalArgumentException("Assessment cannot be empty");
        }
        this.recommendedActions = recommendedActions;
        this.warningSigns = warningSigns;
        this.inPersonVisitRecommended = inPersonVisitRecommended;
        this.urgency = Objects.requireNonNull(urgency, "Urgency level cannot be null");
        this.submittedAt = Instant.now();
    }
}
