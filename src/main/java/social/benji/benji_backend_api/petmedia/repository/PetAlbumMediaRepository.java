package social.benji.benji_backend_api.petmedia.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import social.benji.benji_backend_api.petmedia.domain.PetAlbumMedia;

public interface PetAlbumMediaRepository extends JpaRepository<PetAlbumMedia, UUID> {

    List<PetAlbumMedia> findByAlbumIdOrderByCreatedAtDesc(UUID albumId);

    long countByAlbumId(UUID albumId);
}
