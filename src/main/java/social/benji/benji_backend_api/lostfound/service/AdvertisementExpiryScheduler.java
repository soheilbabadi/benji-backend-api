package social.benji.benji_backend_api.lostfound.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementStatus;
import social.benji.benji_backend_api.lostfound.domain.LostFoundAdvertisementEntity;
import social.benji.benji_backend_api.lostfound.repository.LostFoundAdvertisementRepository;

import java.time.Instant;
import java.util.List;

/** Confirmed rule: ACTIVE advertisements expire exactly 30 days after publication. */
@Component
class AdvertisementExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(AdvertisementExpiryScheduler.class);

    private final LostFoundAdvertisementRepository advertisements;

    AdvertisementExpiryScheduler(LostFoundAdvertisementRepository advertisements) {
        this.advertisements = advertisements;
    }

    @Scheduled(fixedDelayString = "${benji.lostfound.expiry-scan-ms:3600000}")
    @Transactional
    public void expireDueAdvertisements() {
        Instant now = Instant.now();
        List<LostFoundAdvertisementEntity> due =
                advertisements.findByStatusAndExpiresAtBefore(AdvertisementStatus.ACTIVE, now);
        for (LostFoundAdvertisementEntity ad : due) {
            ad.expireIfDue(now); // re-checks status/expiry so concurrent resolves win safely
        }
        if (!due.isEmpty()) {
            advertisements.saveAll(due);
            log.info("Expired {} lost&found advertisements", due.size());
        }
    }
}
