package social.benji.benji_backend_api.consultation.domain.model;

import lombok.Getter;
import social.benji.benji_backend_api.consultation.domain.valueobject.*;

import java.time.Instant;
import java.util.*;

/**
 * Consultation aggregate root.
 * Represents a consultation request from a pet owner to an expert.
 * 
 * Enforces business rules for state transitions and data integrity.
 */
@Getter
public class Consultation {

    private final UUID id;
    private UUID ownerId;
    private UUID petId;
    private ConsultationCategory category;
    private String subject;
    private String question;
    private ConsultationStatus status;
    private Money price;
    private boolean emergencyDisclaimerAccepted;
    private UUID assignedExpertId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;
    private Instant answeredAt;
    private long version;

    // Child collections
    private final Set<PetDataType> sharedPetDataTypes;
    private final List<UUID> attachmentIds;
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

    protected Consultation() {
        // For JPA
        this.id = null;
        this.sharedPetDataTypes = new HashSet<>();
        this.attachmentIds = new ArrayList<>();
    }

    public Consultation(UUID ownerId, UUID petId, ConsultationCategory category, 
                       String subject, String question, Money price) {
        this.id = UUID.randomUUID();
        this.ownerId = Objects.requireNonNull(ownerId, "Owner ID cannot be null");
        this.petId = Objects.requireNonNull(petId, "Pet ID cannot be null");
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.subject = Objects.requireNonNull(subject, "Subject cannot be null");
        if (subject.length() > 255) {
            throw new IllegalArgumentException("Subject cannot exceed 255 characters");
        }
        this.question = Objects.requireNonNull(question, "Question cannot be null");
        if (question.isBlank()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }
        this.status = ConsultationStatus.DRAFT;
        this.price = Objects.requireNonNull(price, "Price cannot be null");
        this.emergencyDisclaimerAccepted = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.sharedPetDataTypes = new HashSet<>();
        this.attachmentIds = new ArrayList<>();
    }

    /**
     * Accepts the emergency disclaimer.
     * Must be called before payment can be initiated.
     */
    public void acceptEmergencyDisclaimer() {
        this.emergencyDisclaimerAccepted = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Adds a pet data type to be shared with the expert.
     * Can only be modified in DRAFT status.
     */
    public void addSharedPetDataType(PetDataType dataType) {
        validateMutableState();
        sharedPetDataTypes.add(dataType);
        this.updatedAt = Instant.now();
    }

    /**
     * Removes a pet data type from sharing.
     * Can only be modified in DRAFT status.
     */
    public void removeSharedPetDataType(PetDataType dataType) {
        validateMutableState();
        sharedPetDataTypes.remove(dataType);
        this.updatedAt = Instant.now();
    }

    /**
     * Adds an attachment reference.
     */
    public void addAttachment(UUID attachmentId) {
        validateMutableState();
        attachmentIds.add(attachmentId);
        this.updatedAt = Instant.now();
    }

    /**
     * Removes an attachment reference.
     */
    public void removeAttachment(UUID attachmentId) {
        validateMutableState();
        attachmentIds.remove(attachmentId);
        this.updatedAt = Instant.now();
    }

    /**
     * Transitions to PAYMENT_PENDING status.
     */
    public void initiatePayment() {
        validateTransition(ConsultationStatus.PAYMENT_PENDING);
        if (!emergencyDisclaimerAccepted) {
            throw new IllegalStateException("Emergency disclaimer must be accepted before payment");
        }
        this.status = ConsultationStatus.PAYMENT_PENDING;
        this.updatedAt = Instant.now();
    }

    /**
     * Transitions to SUBMITTED status after successful payment.
     */
    public void submitAfterPayment() {
        validateTransition(ConsultationStatus.SUBMITTED);
        this.status = ConsultationStatus.SUBMITTED;
        this.updatedAt = Instant.now();
    }

    /**
     * Marks payment as failed.
     */
    public void markPaymentFailed() {
        validateTransition(ConsultationStatus.PAYMENT_FAILED);
        this.status = ConsultationStatus.PAYMENT_FAILED;
        this.updatedAt = Instant.now();
    }

    /**
     * Assigns an expert to this consultation.
     * Can only be done when status is SUBMITTED.
     */
    public void assignExpert(UUID expertId) {
        if (this.status != ConsultationStatus.SUBMITTED) {
            throw new IllegalStateException("Can only assign expert when consultation is SUBMITTED");
        }
        this.assignedExpertId = Objects.requireNonNull(expertId, "Expert ID cannot be null");
        this.status = ConsultationStatus.UNDER_REVIEW;
        this.updatedAt = Instant.now();
    }

    /**
     * Requests additional information from the user.
     */
    public void requestMoreInformation() {
        validateTransition(ConsultationStatus.WAITING_FOR_USER);
        this.status = ConsultationStatus.WAITING_FOR_USER;
        this.updatedAt = Instant.now();
    }

    /**
     * Submits the final answer.
     */
    public void submitAnswer(ConsultationAnswer answer) {
        validateTransition(ConsultationStatus.ANSWERED);
        if (this.answer != null) {
            throw new IllegalStateException("Answer already submitted");
        }
        this.answer = Objects.requireNonNull(answer, "Answer cannot be null");
        this.status = ConsultationStatus.ANSWERED;
        this.answeredAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Closes the consultation.
     */
    public void close() {
        validateTransition(ConsultationStatus.CLOSED);
        this.status = ConsultationStatus.CLOSED;
        this.closedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Cancels the consultation.
     */
    public void cancel() {
        validateTransition(ConsultationStatus.CANCELLED);
        this.status = ConsultationStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    private void validateTransition(ConsultationStatus targetStatus) {
        Set<ConsultationStatus> allowedTransitions = VALID_TRANSITIONS.get(this.status);
        if (allowedTransitions == null || !allowedTransitions.contains(targetStatus)) {
            throw new IllegalStateException(
                String.format("Invalid transition from %s to %s", this.status, targetStatus));
        }
    }

    private void validateMutableState() {
        if (this.status != ConsultationStatus.DRAFT) {
            throw new IllegalStateException("Consultation can only be modified in DRAFT status");
        }
    }

    public boolean isAssignedToExpert(UUID expertId) {
        return this.assignedExpertId != null && this.assignedExpertId.equals(expertId);
    }

    public boolean isOwnedBy(UUID userId) {
        return this.ownerId.equals(userId);
    }

    public boolean canUserAccess(UUID userId) {
        return isOwnedBy(userId) || isAssignedToExpert(userId);
    }
}
