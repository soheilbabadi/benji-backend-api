package social.benji.benji_backend_api.pet.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import social.benji.benji_backend_api.pet.domain.PetPhoto;

public interface PetPhotoRepository extends JpaRepository<PetPhoto, UUID> {

    Optional<PetPhoto> findFirstByPetIdAndPrimaryIsTrue(UUID petId);

    List<PetPhoto> findByPetId(UUID petId);
}
