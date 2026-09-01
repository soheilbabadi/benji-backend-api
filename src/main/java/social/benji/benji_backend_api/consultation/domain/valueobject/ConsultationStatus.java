package social.benji.benji_backend_api.consultation.domain.valueobject;

/**
 * Represents the lifecycle status of a consultation request.
 * 
 * State transitions:
 * DRAFT → PAYMENT_PENDING → SUBMITTED → UNDER_REVIEW → WAITING_FOR_USER → UNDER_REVIEW → ANSWERED → CLOSED
 * 
 * Invalid transitions are rejected at the application service level.
 */
public enum ConsultationStatus {
    DRAFT,              // Initial state when consultation is being created
    PAYMENT_PENDING,    // Payment process initiated but not completed
    SUBMITTED,          // Payment successful, consultation available to experts
    UNDER_REVIEW,       // Expert is reviewing the consultation
    WAITING_FOR_USER,   // Expert requested additional information from user
    ANSWERED,           // Expert submitted final answer
    CLOSED,             // Consultation completed/closed
    CANCELLED,          // Consultation cancelled by user or admin
    PAYMENT_FAILED      // Payment attempt failed
}
