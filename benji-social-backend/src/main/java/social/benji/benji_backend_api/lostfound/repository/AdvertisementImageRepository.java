package social.benji.benji_backend_api.lostfound.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementImageEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvertisementImageRepository extends JpaRepository<AdvertisementImageEntity, UUID> {

    List<AdvertisementImageEntity> findByAdvertisementIdOrderBySortOrderAsc(UUID advertisementId);

    Optional<AdvertisementImageEntity> findByIdAndAdvertisementId(UUID id, UUID advertisementId);

    long countByAdvertisementId(UUID advertisementId);
}
