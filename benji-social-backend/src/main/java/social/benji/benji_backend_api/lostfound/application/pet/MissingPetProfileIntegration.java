package social.benji.benji_backend_api.lostfound.application.pet;

import java.util.UUID;

public class MissingPetProfileIntegration implements PetOwnershipValidator {
    @Override
    public boolean validatePetOwnership(UUID petId, String ownerId) {
        if (petId == null) {
            return true;
        }
        throw new IllegalStateException(
                "Pet Profile ownership integration is not implemented");
    }
}
