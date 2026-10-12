package social.benji.benji_backend_api.lostfound.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Image metadata for an advertisement. The binary lives ONLY in MinIO (private bucket);
 * this row stores the backend-generated object key. Object key format:
 * {@code lostfound/{advertisementId}/{imageId}.{ext}} — client filenames are never used.
 */
@Entity
@Table(name = "lost_found_advertisement_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertisementImageEntity {

    public static final int MAX_IMAGES_PER_ADVERTISEMENT = 5;   // confirmed limit
    public static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024; // confirmed 5 MB

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "advertisement_id", nullable = false, updatable = false)
    private UUID advertisementId;

    @Column(name = "object_key", nullable = false, updatable = false, unique = true, length = 512)
    private String objectKey;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public static String objectKeyFor(UUID advertisementId, UUID imageId, String extension) {
        return "lostfound/" + advertisementId + "/" + imageId + "." + extension;
    }
}
