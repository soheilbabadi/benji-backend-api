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
 * Audit row written each time a visitor reveals the advertiser's masked phone via
 * POST /api/public/lost-found/ads/{publicId}/contact (approved mask/reveal privacy model).
 * Enables abuse review without storing any message content.
 */
@Entity
@Table(name = "lost_found_contact_reveal_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRevealLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "advertisement_id", nullable = false, updatable = false)
    private UUID advertisementId;

    /** Null for anonymous visitors. */
    @Column(name = "viewer_user_id")
    private UUID viewerUserId;

    @Column(name = "viewer_ip", length = 45)
    private String viewerIp;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
