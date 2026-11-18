package social.benji.benji_backend_api.lostfound.application.pet;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MissingPetProfileIntegrationTest {
    @Test
    void acceptsNullPetId() {
        MissingPetProfileIntegration integration = new MissingPetProfileIntegration();
        assertTrue(integration.validatePetOwnership(null, "owner-1"));
    }

    @Test
    void rejectsNonNullPetIdWithoutRealValidator() {
        MissingPetProfileIntegration integration = new MissingPetProfileIntegration();
        assertThrows(IllegalStateException.class,
                () -> integration.validatePetOwnership(UUID.randomUUID(), "owner-1"));
    }
}
