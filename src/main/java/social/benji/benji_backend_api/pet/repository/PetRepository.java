package social.benji.benji_backend_api.pet.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import social.benji.benji_backend_api.pet.domain.Pet;
import social.benji.benji_backend_api.pet.domain.PetStatus;

public interface PetRepository extends JpaRepository<Pet, UUID> {

    List<Pet> findByOwnerIdAndStatusOrderByCreatedAtDesc(UUID ownerId, PetStatus status);

    List<Pet> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    /**
     * Ownership scoping happens inside the query itself so a pet belonging to
     * another user is indistinguishable from a nonexistent one (no id
     * enumeration).
     */
    Optional<Pet> findByIdAndOwnerId(UUID id, UUID ownerId);
}
