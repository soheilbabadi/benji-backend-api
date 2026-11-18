package social.benji.benji_backend_api.lostfound.application.pet;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
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
