package social.benji.benji_backend_api.lostfound.application.pet;

import java.util.UUID;

public interface PetOwnershipValidator {
    boolean validatePetOwnership(UUID petId, String ownerId);
}
