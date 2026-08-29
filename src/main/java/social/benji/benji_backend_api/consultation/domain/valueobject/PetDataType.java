package social.benji.benji_backend_api.consultation.domain.valueobject;

/**
 * Types of pet data that can be shared with an expert.
 * Owners explicitly control which information is shared per consultation.
 */
public enum PetDataType {
    BASIC_INFO,
    AGE,
    BREED,
    GENDER,
    WEIGHT_HISTORY,
    MEDICAL_HISTORY,
    ALLERGIES,
    MEDICATIONS,
    VACCINATIONS,
    RECENT_EVENTS,
    TIMELINE
}
