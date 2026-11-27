package social.benji.benji_backend_api.petmedia.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import social.benji.benji_backend_api.petmedia.domain.PetAlbum;

public interface PetAlbumRepository extends JpaRepository<PetAlbum, UUID> {

    List<PetAlbum> findByPetIdOrderByCreatedAtDesc(UUID petId);
}
