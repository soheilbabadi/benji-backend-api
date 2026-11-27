package social.benji.benji_backend_api.lostfound.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementRejectionHistoryEntity;

import java.util.List;
import java.util.UUID;

public interface AdvertisementRejectionHistoryRepository
        extends JpaRepository<AdvertisementRejectionHistoryEntity, UUID> {
    List<AdvertisementRejectionHistoryEntity> findByAdvertisementIdOrderByRejectionSequenceAsc(UUID advertisementId);
    long countByAdvertisementId(UUID advertisementId);
    void deleteByAdvertisementId(UUID advertisementId);
}
