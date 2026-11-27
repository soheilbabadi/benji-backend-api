package social.benji.benji_backend_api.lostfound.service;

import org.springframework.stereotype.Component;
import social.benji.benji_backend_api.lostfound.application.pet.PetOwnershipValidator;

import java.util.UUID;

@Component
public class PetOwnershipValidatorAdapter implements PetOwnershipValidator {
    private final PetOwnershipPort petOwnershipPort;

    PetOwnershipValidatorAdapter(PetOwnershipPort petOwnershipPort) {
        this.petOwnershipPort = petOwnershipPort;
    }

    @Override
    public boolean validatePetOwnership(UUID petId, String ownerId) {
        if (petId == null) {
            return true;
        }
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalStateException("Authenticated owner identity is required");
        }
        try {
            return petOwnershipPort.ownsPet(petId, UUID.fromString(ownerId));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Authenticated owner identity is malformed", e);
        }
    }
}
