package social.benji.benji_backend_api.consultation.domain.valueobject;

/**
 * Expert verification status.
 * Only VERIFIED and ACTIVE experts can receive consultation requests.
 */
public enum ExpertVerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
