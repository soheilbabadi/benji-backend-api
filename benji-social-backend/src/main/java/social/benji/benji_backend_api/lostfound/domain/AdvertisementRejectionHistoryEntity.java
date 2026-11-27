package social.benji.benji_backend_api.lostfound.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.Instant;
import java.util.UUID;

@Entity
@Audited
@Table(name = "lost_found_advertisement_rejection_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdvertisementRejectionHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "advertisement_id", nullable = false, updatable = false)
    private UUID advertisementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "advertisement_type", nullable = false, length = 10, updatable = false)
    private AdvertisementType advertisementType;

    @Column(name = "administrator_user_id", nullable = false, updatable = false)
    private UUID administratorUserId;

    @Column(name = "rejection_reason", nullable = false, length = 1000, updatable = false)
    private String rejectionReason;

    @Column(name = "rejection_sequence", nullable = false, updatable = false)
    private int rejectionSequence;

    @Column(name = "rejected_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant rejectedAt = Instant.now();
}
