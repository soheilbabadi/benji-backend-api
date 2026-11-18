package social.benji.benji_backend_api.consultation.domain.valueobject;

import lombok.Getter;

/**
 * Urgency levels for expert final answers.
 */
@Getter
public enum UrgencyLevel {
    NO_URGENT_ACTION("بدون اقدام فوری"),
    MONITOR("نظارت"),
    IN_PERSON_VISIT_RECOMMENDED("توصیه به مراجعه حضوری"),
    URGENT_VETERINARY_VISIT("مراجعه فوری به دامپزشک");

    private final String value;

    UrgencyLevel(String value) {
        this.value = value;
    }

    public static UrgencyLevel fromString(String input) {
        for (UrgencyLevel type : UrgencyLevel.values()) {
            if (type.name().equalsIgnoreCase(input) || type.value.equals(input)) {
                return type;
            }
        }
        return null;
    }
}
