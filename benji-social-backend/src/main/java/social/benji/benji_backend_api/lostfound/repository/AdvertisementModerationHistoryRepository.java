package social.benji.benji_backend_api.lostfound.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementModerationHistoryEntity;

import java.util.UUID;

public interface AdvertisementModerationHistoryRepository
        extends JpaRepository<AdvertisementModerationHistoryEntity, UUID> {
    void deleteByAdvertisementId(UUID advertisementId);
}
