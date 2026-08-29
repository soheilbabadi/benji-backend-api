package social.benji.benji_backend_api.consultation.domain.valueobject;

/**
 * Payment status for consultation payments.
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED,
    CANCELLED
}
