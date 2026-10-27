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
import social.benji.benji_backend_api.exception.BenjiCustomException;

import java.math.BigDecimal;
import java.time.Duration;
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
 *   <li>Status changes go exclusively through {@link #transitionTo}; activation of a LOST ad can
 *       only be triggered by the module-internal payment-success handler, never by an HTTP call.</li>
 *   <li>Contact phone is ad-specific data explicitly provided by the advertiser; public reads get
 *       a MASKED value ({@link #maskedContactPhone()}); full value only via the reveal endpoint.</li>
 * </ul>
 */
@Entity
@Table(name = "lost_found_advertisement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LostFoundAdvertisementEntity {

    /** Confirmed expiry duration: ACTIVE ads expire 30 days after publication. */
    public static final Duration ADVERTISEMENT_TTL = Duration.ofDays(30);

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
    private AdvertisementStatus status = AdvertisementStatus.DRAFT;

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
            throw new BenjiCustomException("LOST advertisement requires a pet reference");
        }
        if (advertisementType == AdvertisementType.FOUND && petId != null) {
            throw new BenjiCustomException("FOUND advertisement must not reference a pet profile");
        }
    }

    // ---- Lifecycle ---------------------------------------------------------------------------

    /** The ONLY way status changes. Rejects anything outside {@link AdvertisementStatus#canTransitionTo}. */
    public void transitionTo(AdvertisementStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new BenjiCustomException(
                    "Illegal advertisement transition " + status + " -> " + next + " for ad " + publicId);
        }
        this.status = next;
        applySideEffects(next);
    }

    private void applySideEffects(AdvertisementStatus next) {
        Instant now = Instant.now();
        switch (next) {
            case ACTIVE -> {
                if (publishedAt == null) {
                    publishedAt = now;
                }
                expiresAt = publishedAt.plus(ADVERTISEMENT_TTL); // confirmed: 30 days
                hiddenReason = null;
            }
            case RESOLVED -> {
                resolvedAt = now;
                expiresAt = null;
            }
            case CLOSED -> closedAt = now;
            default -> { }
        }
    }

    /** Called ONLY by the payment-success event handler (server-verified). Idempotent no-op otherwise. */
    public void activateOnVerifiedPayment(UUID actingUserId) {
        if (advertisementType != AdvertisementType.LOST) {
            throw new BenjiCustomException("Only LOST advertisements require payment activation");
        }
        if (!userId.equals(actingUserId)) {
            throw new BenjiCustomException("Payment event user does not match advertisement owner");
        }
        if (status == AdvertisementStatus.ACTIVE) {
            return; // duplicate PaymentSucceededEvent → harmless no-op (idempotency requirement)
        }
        if (status != AdvertisementStatus.PAYMENT_PENDING) {
            // e.g. payment succeeded after the draft was CANCELLED: record stays in Payment module
            // for refund review; the ad is NOT resurrected.
            throw new BenjiCustomException("Advertisement is not awaiting payment: " + status);
        }
        transitionTo(AdvertisementStatus.ACTIVE);
    }

    public void markResolved(UUID resolverUserId, String note) {
        this.resolvedByUserId = resolverUserId;
        this.resolutionNote = note;
        transitionTo(AdvertisementStatus.RESOLVED);
    }

    /** Scheduler-only transition; guarded so it can never fire on non-ACTIVE rows. */
    public void expireIfDue(Instant now) {
        if (status == AdvertisementStatus.ACTIVE && expiresAt != null && !expiresAt.isAfter(now)) {
            transitionTo(AdvertisementStatus.EXPIRED);
        }
    }

    public void hide(String reason) {
        this.hiddenReason = reason;
        transitionTo(AdvertisementStatus.HIDDEN);
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
