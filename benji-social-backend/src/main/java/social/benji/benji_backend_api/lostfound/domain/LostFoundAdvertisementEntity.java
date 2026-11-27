package social.benji.benji_backend_api.lostfound.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.lostfound.domain.lifecycle.FoundAdvertisementLifecycle;
import social.benji.benji_backend_api.lostfound.domain.lifecycle.LostAdvertisementLifecycle;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A LOST or FOUND advertisement. Single shared entity for both types (approved design): the
 * type-conditional logic is small and localized — petId required for LOST, forbidden for FOUND —
 * and enforced here AND by DB CHECK constraints (lostfound changelog 001).
 *
 * <p>Boundary rules:</p>
 * <ul>
 *   <li>userId / petId / breedId are bare UUIDs with NO foreign keys to other modules' tables.</li>
 *   <li>Pet ownership is verified server-side at creation via a port — never trusted from the client.</li>
 *   <li>Status changes go exclusively through independent LOST/FOUND lifecycle methods; activation of a LOST ad can
 *       only be triggered by the module-internal payment-success handler, never by an HTTP call.</li>
 *   <li>Contact phone is ad-specific data explicitly provided by the advertiser; public reads get
 *       a MASKED value ({@link #maskedContactPhone()}); full value only via the reveal endpoint.</li>
 * </ul>
 */
@Entity
@Audited
@Table(name = "lost_found_advertisement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LostFoundAdvertisementEntity {

    /** Approved publication duration: exactly 30 x 24 hours, measured with Instant plusSeconds. */
    public static final long ADVERTISEMENT_TTL_SECONDS = 30L * 24L * 60L * 60L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Client-facing identifier used in all public REST paths; internal PK never exposed. */
    @Column(name = "public_id", updatable = false, nullable = false, unique = true)
    @Builder.Default
    private UUID publicId = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(name = "advertisement_type", nullable = false, length = 10)
    private AdvertisementType advertisementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AdvertisementStatus status = AdvertisementStatus.UNDER_REVIEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "lost_lifecycle_status", length = 20)
    private LostAdvertisementLifecycle lostLifecycleStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "found_lifecycle_status", length = 20)
    private FoundAdvertisementLifecycle foundLifecycleStatus;

    /** Advertiser. No FK to user module (modular-monolith boundary). */
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /** Required for LOST (owner verified server-side), always null for FOUND. */
    @Column(name = "pet_id")
    private UUID petId;

    @Enumerated(EnumType.STRING)
    @Column(name = "species", nullable = false, length = 10)
    private Species species;

    /** Nullable: mixed/unknown breeds must not block reporting. Reference-data id, no FK. */
    @Column(name = "breed_id")
    private UUID breedId;

    @Column(name = "animal_name", length = 100)
    private String animalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = false, length = 10)
    @Builder.Default
    private AnimalSex sex = AnimalSex.UNKNOWN;

    @Column(name = "color", length = 100)
    private String color;

    @Column(name = "distinguishing_characteristics", length = 1000)
    private String distinguishingCharacteristics;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "incident_at", nullable = false)
    private Instant incidentAt;

    @Column(name = "country", nullable = false, length = 64)
    private String country;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "area", length = 150)
    private String area;

    /** Approximate INCIDENT location only — never the advertiser's home address. */
    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_precision", nullable = false, length = 20)
    @Builder.Default
    private LocationPrecision locationPrecision = LocationPrecision.CITY;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_method", nullable = false, length = 10)
    private ContactMethod contactMethod;

    /** E.164 phone provided FOR THIS AD. Masked on public reads until revealed via /contact. */
    @Column(name = "contact_phone_e164", length = 20)
    private String contactPhoneE164;

    /** Advertiser-declared reward in integer IRR Rials; informational, Benji never processes it. */
    @Column(name = "reward_amount_irr", precision = 19, scale = 0)
    private BigDecimal rewardAmountIrr;

    @Column(name = "include_in_search", nullable = false)
    @Builder.Default
    private boolean includeInSearch = true;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "payment_deadline_at")
    private Instant paymentDeadlineAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "resubmitted_at")
    private Instant resubmittedAt;

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by_user_id")
    private UUID resolvedByUserId;

    @Column(name = "resolution_note", length = 500)
    private String resolutionNote;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "hidden_reason", length = 500)
    private String hiddenReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Optimistic locking: concurrent edits / double-submit surface as 409, not silent overwrite. */
    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // ---- Type invariant (defense-in-depth over the DB CHECK constraints) ---------------------

    public void assertTypeConsistency() {
        if (advertisementType == AdvertisementType.LOST && petId == null) {
            throw new BenjiCustomException.ValidationException("LOST advertisement requires a pet reference");
        }
        if (advertisementType == AdvertisementType.FOUND && petId != null) {
            throw new BenjiCustomException.ValidationException("FOUND advertisement must not reference a pet profile");
        }
    }

    public void enterModeration() {
        assertTypeConsistency();
        Instant now = Instant.now();
        updatedAt = now;
        if (advertisementType == AdvertisementType.LOST) {
            lostLifecycleStatus = LostAdvertisementLifecycle.UNDER_REVIEW;
            foundLifecycleStatus = null;
            status = AdvertisementStatus.UNDER_REVIEW;
        } else {
            foundLifecycleStatus = FoundAdvertisementLifecycle.UNDER_REVIEW;
            lostLifecycleStatus = null;
            status = AdvertisementStatus.UNDER_REVIEW;
        }
    }

    public void rejectLostOrFound(Instant now) {
        if (advertisementType == AdvertisementType.LOST) {
            LostAdvertisementLifecycle current = lostLifecycleStatus;
            if (current != LostAdvertisementLifecycle.UNDER_REVIEW
                    || !current.canTransitionTo(LostAdvertisementLifecycle.REJECTED)) {
                throw new BenjiCustomException.ValidationException("Lost advertisement cannot be rejected");
            }
            lostLifecycleStatus = LostAdvertisementLifecycle.REJECTED;
        } else {
            FoundAdvertisementLifecycle current = foundLifecycleStatus;
            if (current != FoundAdvertisementLifecycle.UNDER_REVIEW
                    || !current.canTransitionTo(FoundAdvertisementLifecycle.REJECTED)) {
                throw new BenjiCustomException.ValidationException("Found advertisement cannot be rejected");
            }
            foundLifecycleStatus = FoundAdvertisementLifecycle.REJECTED;
        }
        status = AdvertisementStatus.REJECTED;
        rejectedAt = now;
        updatedAt = now;
    }

    public void resubmit(Instant now) {
        if (advertisementType == AdvertisementType.LOST) {
            if (lostLifecycleStatus != LostAdvertisementLifecycle.REJECTED) {
                throw new BenjiCustomException.ValidationException("Only rejected Lost advertisements can be resubmitted");
            }
            lostLifecycleStatus = LostAdvertisementLifecycle.UNDER_REVIEW;
        } else {
            if (foundLifecycleStatus != FoundAdvertisementLifecycle.REJECTED) {
                throw new BenjiCustomException.ValidationException("Only rejected Found advertisements can be resubmitted");
            }
            foundLifecycleStatus = FoundAdvertisementLifecycle.UNDER_REVIEW;
        }
        status = AdvertisementStatus.UNDER_REVIEW;
        resubmittedAt = now;
        updatedAt = now;
    }

    public void approveLost(Instant now) {
        if (advertisementType != AdvertisementType.LOST
                || lostLifecycleStatus != LostAdvertisementLifecycle.UNDER_REVIEW) {
            throw new BenjiCustomException.ValidationException("Lost advertisement cannot be approved");
        }
        lostLifecycleStatus = LostAdvertisementLifecycle.PAYMENT_PENDING;
        status = AdvertisementStatus.PAYMENT_PENDING;
        approvedAt = now;
        paymentDeadlineAt = now.plusSeconds(48L * 60L * 60L);
        updatedAt = now;
    }

    public void approveFound(Instant now) {
        if (advertisementType != AdvertisementType.FOUND
                || foundLifecycleStatus != FoundAdvertisementLifecycle.UNDER_REVIEW) {
            throw new BenjiCustomException.ValidationException("Found advertisement cannot be approved");
        }
        foundLifecycleStatus = FoundAdvertisementLifecycle.PUBLISHED;
        status = AdvertisementStatus.PUBLISHED;
        approvedAt = now;
        publishedAt = now;
        expiresAt = now.plusSeconds(ADVERTISEMENT_TTL_SECONDS);
        updatedAt = now;
    }

    public void publishLostAfterVerifiedPayment(Instant now) {
        if (advertisementType != AdvertisementType.LOST
                || lostLifecycleStatus != LostAdvertisementLifecycle.PAYMENT_PENDING) {
            throw new BenjiCustomException.ValidationException("Lost advertisement is not awaiting verified payment");
        }
        lostLifecycleStatus = LostAdvertisementLifecycle.PUBLISHED;
        status = AdvertisementStatus.PUBLISHED;
        publishedAt = now;
        expiresAt = now.plusSeconds(ADVERTISEMENT_TTL_SECONDS);
        updatedAt = now;
    }

    public void expire(Instant now) {
        if (expiresAt == null || now.isBefore(expiresAt)) {
            throw new BenjiCustomException.ValidationException("Advertisement has not reached expiry");
        }
        if (advertisementType == AdvertisementType.LOST
                && lostLifecycleStatus == LostAdvertisementLifecycle.PUBLISHED) {
            lostLifecycleStatus = LostAdvertisementLifecycle.EXPIRED;
            status = AdvertisementStatus.EXPIRED;
        } else if (advertisementType == AdvertisementType.FOUND
                && foundLifecycleStatus == FoundAdvertisementLifecycle.PUBLISHED) {
            foundLifecycleStatus = FoundAdvertisementLifecycle.EXPIRED;
            status = AdvertisementStatus.EXPIRED;
        } else {
            throw new BenjiCustomException.ValidationException("Advertisement cannot expire");
        }
        updatedAt = now;
    }

    public void deactivate(UUID administratorId, String reason, Instant now) {
        if (reason == null || reason.isBlank()) {
            throw new BenjiCustomException.ValidationException("Deactivation reason is required");
        }
        if (advertisementType == AdvertisementType.LOST
                && lostLifecycleStatus == LostAdvertisementLifecycle.PUBLISHED) {
            lostLifecycleStatus = LostAdvertisementLifecycle.DEACTIVATED;
        } else if (advertisementType == AdvertisementType.FOUND
                && foundLifecycleStatus == FoundAdvertisementLifecycle.PUBLISHED) {
            foundLifecycleStatus = FoundAdvertisementLifecycle.DEACTIVATED;
        } else {
            throw new BenjiCustomException.ValidationException("Only published advertisements can be deactivated");
        }
        status = AdvertisementStatus.DEACTIVATED;
        hiddenReason = reason;
        deactivatedAt = now;
        updatedAt = now;
    }

    // ---- Independent lifecycle transitions -----------------------------------------------

    /** Called only by the verified Payment-success event handler. Duplicate delivery is a no-op. */
    public void activateOnVerifiedPayment(UUID actingUserId) {
        if (advertisementType != AdvertisementType.LOST) {
            throw new BenjiCustomException.ValidationException("Only LOST advertisements require payment activation");
        }
        if (!userId.equals(actingUserId)) {
            throw new BenjiCustomException.ValidationException("Payment event user does not match advertisement owner");
        }
        if (lostLifecycleStatus == LostAdvertisementLifecycle.PUBLISHED) {
            return;
        }
        publishLostAfterVerifiedPayment(Instant.now());
    }

    public void markResolved(UUID resolverUserId, String note) {
        if (!userId.equals(resolverUserId)) {
            throw new BenjiCustomException.ResourceUnauthorizedException("Only the owner can resolve an advertisement");
        }
        resolvedByUserId = resolverUserId;
        resolutionNote = note;
        resolvedAt = Instant.now();
    }

    public void expireIfDue(Instant now) {
        expire(now);
    }

    public void hide(String reason) {
        deactivate(null, reason, Instant.now());
    }

    // ---- Privacy helpers -----------------------------------------------------------------------

    /** "+98912••••12" style mask used on ALL public DTOs. Never leaks the full number. */
    public String maskedContactPhone() {
        String p = contactPhoneE164;
        if (p == null || p.length() < 7) {
            return null;
        }
        return p.substring(0, Math.min(5, p.length() - 6)) + "•••" + p.substring(p.length() - 3);
    }

    public boolean isOwnedBy(UUID candidateUserId) {
        return userId != null && userId.equals(candidateUserId);
    }
}
