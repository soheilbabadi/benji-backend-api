package social.benji.benji_backend_api.pet.domain;

/**
 * Pet lifecycle. Physical deletion is intentionally not supported so future
 * Pet Life Record data can never be orphaned by a casual delete.
 *
 * <p>LOST and FOUND are profile-state markers only. The Lost &amp; Found
 * advertisement module owns its own independent state machine
 * ({@code AdvertisementStatus}); these statuses must never be conflated with
 * ad publication/resolution, and setting them does not create or modify any
 * advertisement.</p>
 */
public enum PetStatus {
    ACTIVE,
    LOST,
    FOUND,
    REHOMED,
    ARCHIVED,
    DECEASED;

    public boolean isTerminal() {
        return this == DECEASED || this == REHOMED;
    }

    /** Statuses whose profile fields stay editable (owners may complete them later). */
    public boolean isEditable() {
        return !isTerminal();
    }
}
