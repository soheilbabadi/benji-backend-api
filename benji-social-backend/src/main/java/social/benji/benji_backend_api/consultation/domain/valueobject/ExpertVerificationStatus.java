package social.benji.benji_backend_api.consultation.domain.valueobject;

import lombok.Getter;

/**
 * Expert verification status.
 * Only VERIFIED and ACTIVE experts can receive consultation requests.
 */
@Getter
public enum ExpertVerificationStatus {
    PENDING("در انتظار بررسی"),
    VERIFIED("تایید شده"),
    REJECTED("رد شده");

    private final String value;

    ExpertVerificationStatus(String value) {
        this.value = value;
    }

    public static ExpertVerificationStatus fromString(String input) {
        for (ExpertVerificationStatus type : ExpertVerificationStatus.values()) {
            if (type.name().equalsIgnoreCase(input) || type.value.equals(input)) {
                return type;
            }
        }
        return null;
    }
}
