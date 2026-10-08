package social.benji.benji_backend_api.pet.domain;

/**
 * Pet lifecycle. Physical deletion is intentionally not supported so future
 * Pet Life Record data can never be orphaned by a casual delete.
 */
public enum PetStatus {
    ACTIVE,
    ARCHIVED,
    DECEASED;

    public boolean isTerminal() {
        return this == DECEASED;
    }
}
