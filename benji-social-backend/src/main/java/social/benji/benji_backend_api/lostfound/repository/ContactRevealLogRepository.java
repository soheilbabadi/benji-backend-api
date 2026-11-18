package social.benji.benji_backend_api.lostfound.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.lostfound.domain.ContactRevealLogEntity;

import java.time.Instant;
import java.util.UUID;

public interface ContactRevealLogRepository extends JpaRepository<ContactRevealLogEntity, UUID> {

    /** Abuse-prevention window check before revealing the advertiser's phone. */
    long countByViewerUserIdAndCreatedAtAfter(UUID viewerUserId, Instant since);

    long countByAdvertisementIdAndViewerIpAndCreatedAtAfter(UUID advertisementId, String ip, Instant since);
}
