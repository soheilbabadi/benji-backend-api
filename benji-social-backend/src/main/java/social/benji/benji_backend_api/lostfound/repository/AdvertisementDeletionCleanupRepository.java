package social.benji.benji_backend_api.lostfound.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementDeletionCleanupEntity;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementDeletionStatus;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

public interface AdvertisementDeletionCleanupRepository
        extends JpaRepository<AdvertisementDeletionCleanupEntity, UUID> {
    List<AdvertisementDeletionCleanupEntity> findTop50ByStatusInOrderByCreatedAtAsc(
            List<AdvertisementDeletionStatus> statuses);
    List<AdvertisementDeletionCleanupEntity> findTop50ByStatusAndUpdatedAtBefore(
            AdvertisementDeletionStatus status, Instant updatedBefore);
}
