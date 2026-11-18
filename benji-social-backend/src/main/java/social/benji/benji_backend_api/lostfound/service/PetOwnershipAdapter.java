package social.benji.benji_backend_api.lostfound.service;

import org.springframework.stereotype.Component;
import social.benji.benji_backend_api.pet.repository.PetRepository;

import java.util.UUID;

/** Thin in-process adapter across the pet-module boundary (modular monolith). */
@Component
class PetOwnershipAdapter implements PetOwnershipPort {

    private final PetRepository petRepository;

    PetOwnershipAdapter(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Override
    public boolean ownsPet(UUID petId, UUID ownerId) {
        // Ownership scoping is inside the query: another user's pet is indistinguishable
        // from a nonexistent one (no id enumeration).
        return petId != null && petRepository.findByIdAndOwnerId(petId, ownerId).isPresent();
    }
}
