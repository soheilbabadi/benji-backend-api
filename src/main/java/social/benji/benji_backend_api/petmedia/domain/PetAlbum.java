package social.benji.benji_backend_api.petmedia.domain;

import jakarta.persistence.*;
import lombok.*;
import social.benji.benji_backend_api.petmedia.valueobject.AlbumStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * A named collection of media belonging to exactly one pet. Ownership is not
 * stored here: it is derived through pet_id via the Pet Profile module, keeping
 * a single source of truth for who owns what.
 */
@Entity
@Table(name = "tbl_pet_albums")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetAlbum {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "pet_id", nullable = false, updatable = false)
    private UUID petId;

    @Column(name = "pet_name", nullable = false, length = 100)
    private String petName;

    @Column(name = "description", length = 500)
    private String description;

    /** Must reference a photo inside this same album; enforced in the service. */
    @Column(name = "cover_photo_id")
    private UUID coverPhotoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "album_status", nullable = false, length = 20)
    @Builder.Default
    private AlbumStatus albumStatus = AlbumStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
