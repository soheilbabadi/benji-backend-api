package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import social.benji.benji_backend_api.consultation.domain.valueobject.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Consultation aggregate root.
 * Represents a consultation request from a pet owner to an expert.
 *
 * Enforces business rules for state transitions and data integrity.
 */
@Document(collection = "consultations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultation {

    @Id
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
    @Version
    private long version;

    private Set<PetDataType> sharedPetDataTypes;
    private List<String> attachmentIds;
    private ConsultationAnswer answer;

    // Valid state transitions map
    private static final Map<ConsultationStatus, Set<ConsultationStatus>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put(ConsultationStatus.DRAFT, Set.of(ConsultationStatus.PAYMENT_PENDING, ConsultationStatus.CANCELLED));
        VALID_TRANSITIONS.put(ConsultationStatus.PAYMENT_PENDING, Set.of(ConsultationStatus.SUBMITTED, ConsultationStatus.PAYMENT_FAILED, ConsultationStatus.CANCELLED));
        VALID_TRANSITIONS.put(ConsultationStatus.SUBMITTED, Set.of(ConsultationStatus.UNDER_REVIEW, ConsultationStatus.CANCELLED));
        VALID_TRANSITIONS.put(ConsultationStatus.UNDER_REVIEW, Set.of(ConsultationStatus.WAITING_FOR_USER, ConsultationStatus.ANSWERED, ConsultationStatus.CLOSED));
        VALID_TRANSITIONS.put(ConsultationStatus.WAITING_FOR_USER, Set.of(ConsultationStatus.UNDER_REVIEW, ConsultationStatus.CANCELLED));
        VALID_TRANSITIONS.put(ConsultationStatus.ANSWERED, Set.of(ConsultationStatus.CLOSED));
        VALID_TRANSITIONS.put(ConsultationStatus.CLOSED, Set.of());
        VALID_TRANSITIONS.put(ConsultationStatus.CANCELLED, Set.of());
        VALID_TRANSITIONS.put(ConsultationStatus.PAYMENT_FAILED, Set.of(ConsultationStatus.PAYMENT_PENDING, ConsultationStatus.CANCELLED));
    }
}
