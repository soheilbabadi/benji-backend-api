package social.benji.benji_backend_api.petmedia.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metadata for one object stored in MinIO. Binary content never touches
 * PostgreSQL — only the object key and descriptive fields live here.
 */
@Entity
@Table(name = "tbl_pet_album_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetAlbumMedia {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "album_id", nullable = false)
    private UUID albumId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    @Builder.Default
    private MediaType mediaType = MediaType.PHOTO;

    @Column(name = "object_key", nullable = false, unique = true, length = 255)
    private String objectKey;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    /** Extracted server-side from the decoded image; null when undetectable. */
    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
