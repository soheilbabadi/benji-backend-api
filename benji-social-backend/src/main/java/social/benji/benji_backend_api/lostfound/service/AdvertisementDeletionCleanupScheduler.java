package social.benji.benji_backend_api.lostfound.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementDeletionCleanupEntity;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementDeletionStatus;
import social.benji.benji_backend_api.lostfound.repository.AdvertisementDeletionCleanupRepository;
import social.benji.benji_backend_api.storage.MinioProperties;
import social.benji.benji_backend_api.storage.ObjectStorage;

import java.util.List;
import java.util.ArrayList;
import java.time.Instant;

@Component
public class AdvertisementDeletionCleanupScheduler {
    private static final Logger log =
            LoggerFactory.getLogger(AdvertisementDeletionCleanupScheduler.class);

    private final AdvertisementDeletionCleanupRepository cleanupRepository;
    private final ObjectStorage objectStorage;
    private final MinioProperties minio;
    @Value("${benji.lostfound.deletion-processing-stale-ms:1800000}")
    private long processingStaleMs;

    AdvertisementDeletionCleanupScheduler(
            AdvertisementDeletionCleanupRepository cleanupRepository,
            ObjectStorage objectStorage,
            MinioProperties minio) {
        this.cleanupRepository = cleanupRepository;
        this.objectStorage = objectStorage;
        this.minio = minio;
    }

    @Scheduled(fixedDelayString = "${benji.lostfound.deletion-cleanup-ms:300000}")
    @Transactional
    public void processPendingDeletionCleanup() {
        List<AdvertisementDeletionCleanupEntity> jobs = new ArrayList<>(
                cleanupRepository.findTop50ByStatusInOrderByCreatedAtAsc(
                        List.of(AdvertisementDeletionStatus.PENDING,
                                AdvertisementDeletionStatus.FAILED)));
        Instant staleBefore = Instant.now().minusMillis(processingStaleMs);
        for (AdvertisementDeletionCleanupEntity staleJob :
                cleanupRepository.findTop50ByStatusAndUpdatedAtBefore(
                        AdvertisementDeletionStatus.PROCESSING, staleBefore)) {
            if (jobs.stream().noneMatch(job ->
                    job.getAdvertisementPublicId().equals(staleJob.getAdvertisementPublicId()))) {
                jobs.add(staleJob);
            }
        }
        for (AdvertisementDeletionCleanupEntity job : jobs) {
            job.markProcessing();
            cleanupRepository.save(job);
            try {
                if (job.getImageObjectKeys() != null
                        && !job.getImageObjectKeys().isBlank()) {
                    for (String objectKey : job.getImageObjectKeys().split("\\n")) {
                        if (!objectKey.isBlank()) {
                            objectStorage.delete(minio.bucketName(), objectKey);
                        }
                    }
                }
                cleanupRepository.delete(job);
            } catch (RuntimeException e) {
                job.markFailed(e.getMessage());
                cleanupRepository.save(job);
                log.warn("Advertisement deletion file cleanup failed for {}",
                        job.getAdvertisementPublicId(), e);
            }
        }
    }
}
