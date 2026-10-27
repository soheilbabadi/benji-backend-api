package social.benji.benji_backend_api.consultation.domain.valueobject;

import lombok.Getter;

/**
 * Consultation categories for routing and classification.
 * Extensible design allows new categories to be added later.
 */
@Getter
public enum ConsultationCategory {
    GENERAL_HEALTH("سلامت عمومی"),
    NUTRITION("تغذیه"),
    BEHAVIOR("رفتار"),
    GROOMING_AND_CARE("آراستگی و مراقبت"),
    TRAINING("آموزش"),
    OTHER("سایر");

    private final String value;

    ConsultationCategory(String value) {
        this.value = value;
    }

    public static ConsultationCategory fromString(String input) {
        for (ConsultationCategory type : ConsultationCategory.values()) {
            if (type.name().equalsIgnoreCase(input) || type.value.equals(input)) {
                return type;
            }
        }
        return null;
    }
}
