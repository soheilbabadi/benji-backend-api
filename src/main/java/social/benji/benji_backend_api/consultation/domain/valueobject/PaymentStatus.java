package social.benji.benji_backend_api.consultation.domain.valueobject;

import lombok.Getter;

/**
 * Payment status for consultation payments.
 */
@Getter
public enum PaymentStatus {
    PENDING("در انتظار"),
    SUCCESS("موفق"),
    FAILED("ناموفق"),
    REFUNDED("بازپرداخت شده"),
    CANCELLED("لغو شده");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public static PaymentStatus fromString(String input) {
        for (PaymentStatus type : PaymentStatus.values()) {
            if (type.name().equalsIgnoreCase(input) || type.value.equals(input)) {
                return type;
            }
        }
        return null;
    }
}
