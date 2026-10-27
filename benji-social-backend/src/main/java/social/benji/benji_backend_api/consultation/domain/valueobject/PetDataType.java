package social.benji.benji_backend_api.consultation.domain.valueobject;

import lombok.Getter;

/**
 * Types of pet data that can be shared with an expert.
 * Owners explicitly control which information is shared per consultation.
 */
@Getter
public enum PetDataType {
    BASIC_INFO("اطلاعات پایه"),
    AGE("سن"),
    BREED("نژاد"),
    GENDER("جنسیت"),
    WEIGHT_HISTORY("تاریخچه وزن"),
    MEDICAL_HISTORY("تاریخچه پزشکی"),
    ALLERGIES("حساسیت‌ها"),
    MEDICATIONS("داروها"),
    VACCINATIONS("واکسیناسیون‌ها"),
    RECENT_EVENTS("رویدادهای اخیر"),
    TIMELINE("جدول زمانی");

    private final String value;

    PetDataType(String value) {
        this.value = value;
    }

    public static PetDataType fromString(String input) {
        for (PetDataType type : PetDataType.values()) {
            if (type.name().equalsIgnoreCase(input) || type.value.equals(input)) {
                return type;
            }
        }
        return null;
    }
}
