package social.benji.benji_backend_api.lostfound.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** Minimal MVP moderation record. Reports are immutable once reviewed (audit trail; never deleted). */
@Entity
@Table(name = "lost_found_advertisement_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertisementReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "advertisement_id", nullable = false, updatable = false)
    private UUID advertisementId;

    @Column(name = "reporter_user_id", nullable = false, updatable = false)
    private UUID reporterUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 32)
    private ReportReason reason;

    @Column(name = "details", length = 1000)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private ReportStatus status = ReportStatus.OPEN;

    @Column(name = "reviewed_by_user_id")
    private UUID reviewedByUserId;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public void review(UUID adminUserId, ReportStatus outcome, String note) {
        if (status != ReportStatus.OPEN) {
            throw new IllegalStateException("Report already reviewed");
        }
        if (outcome == ReportStatus.OPEN) {
            throw new IllegalArgumentException("Review outcome must be DISMISSED or ACTIONED");
        }
        this.status = outcome;
        this.reviewedByUserId = adminUserId;
        this.reviewNote = note;
        this.reviewedAt = Instant.now();
    }
}
