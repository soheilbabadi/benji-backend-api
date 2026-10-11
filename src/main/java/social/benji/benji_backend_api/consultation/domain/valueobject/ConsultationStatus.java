package social.benji.benji_backend_api.consultation.domain.valueobject;

import lombok.Getter;

/**
 * Represents the lifecycle status of a consultation request.
 * 
 * State transitions:
 * DRAFT → PAYMENT_PENDING → SUBMITTED → UNDER_REVIEW → WAITING_FOR_USER → UNDER_REVIEW → ANSWERED → CLOSED
 * 
 * Invalid transitions are rejected at the application service level.
 */
@Getter
public enum ConsultationStatus {
    DRAFT("پیش‌نویس"),
    PAYMENT_PENDING("در انتظار پرداخت"),
    SUBMITTED("ثبت شده"),
    UNDER_REVIEW("در حال بررسی"),
    WAITING_FOR_USER("در انتظار کاربر"),
    ANSWERED("پاسخ داده شده"),
    CLOSED("بسته شده"),
    CANCELLED("لغو شده"),
    PAYMENT_FAILED("پرداخت ناموفق");

    private final String value;

    ConsultationStatus(String value) {
        this.value = value;
    }

    public static ConsultationStatus fromString(String input) {
        for (ConsultationStatus type : ConsultationStatus.values()) {
            if (type.name().equalsIgnoreCase(input) || type.value.equals(input)) {
                return type;
            }
        }
        return null;
    }
}
