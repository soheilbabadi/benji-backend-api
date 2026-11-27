package social.benji.benji_backend_api.lostfound.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lost_found_advertisement_deletion_cleanup")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdvertisementDeletionCleanupEntity {
    @Id
    @Column(name = "advertisement_public_id", nullable = false, updatable = false)
    private UUID advertisementPublicId;

    @Column(name = "owner_user_id", nullable = false, updatable = false)
    private UUID ownerUserId;

    @Column(name = "image_object_keys", length = 8192)
    private String imageObjectKeys;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AdvertisementDeletionStatus status = AdvertisementDeletionStatus.PENDING;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private int attemptCount = 0;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private long version;

    public void markProcessing() {
        status = AdvertisementDeletionStatus.PROCESSING;
        attemptCount++;
        updatedAt = Instant.now();
    }

    public void markFailed(String error) {
        status = AdvertisementDeletionStatus.FAILED;
        lastError = error == null ? "Deletion cleanup failed" : error;
        updatedAt = Instant.now();
    }

    public void markPendingRetry() {
        status = AdvertisementDeletionStatus.PENDING;
        updatedAt = Instant.now();
    }
}
