package social.benji.benji_backend_api.pet.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Basic identity of a user's dog or cat. Deliberately free of health, weight
 * history or care data: those belong to future modules that attach records via
 * this stable pet id. Ownership is a bare owner_id reference, never an entity
 * association, to keep the module boundary intact.
 */
@Entity
@Table(name = "tbl_pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "species", nullable = false, length = 20)
    private PetSpecies species;

    /** Nullable: breed is optional at creation (progressive profile completion). */
    @Column(name = "breed_id")
    private UUID breedId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = false, length = 20)
    @Builder.Default
    private PetSex sex = PetSex.UNKNOWN;

    /** Null means the birth date is unknown; age is derived, never stored. */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** True when date_of_birth is an owner estimate rather than a known date. */
    @Column(name = "birth_date_estimated")
    private Boolean birthDateEstimated;

    @Column(name = "color", length = 50)
    private String color;

    /** Profile snapshot only; historical measurements belong to Weight & Growth. */
    @Column(name = "current_weight_kg", precision = 6, scale = 2)
    private BigDecimal currentWeightKg;

    /** Optional standing-height snapshot in cm; history belongs to other modules. */
    @Column(name = "height_cm", precision = 6, scale = 2)
    private BigDecimal heightCm;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_size", length = 20)
    private BodySize bodySize;

    @Column(name = "eye_color", length = 50)
    private String eyeColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "coat_length", length = 20)
    private CoatLength coatLength;

    /** Free-text physical description (markings, distinguishing features). */
    @Column(name = "physical_description", length = 1000)
    private String physicalDescription;

    /** Optional; duplicates across users are tolerated (no unique constraint). */
    @Column(name = "microchip_number", length = 50)
    private String microchipNumber;

    /** Tri-state: null means the owner has not specified it yet. */
    @Column(name = "neutered")
    private Boolean neutered;

    /** Free-text coat colour, kept alongside the legacy generic color field. */
    @Column(name = "coat_color", length = 50)
    private String coatColor;

    /** Optional free-text city; may be left empty and completed later. */
    @Column(name = "city", length = 100)
    private String city;

    /** Short "about the pet" blurb shown on the profile. */
    @Column(name = "bio", length = 500)
    private String bio;

    // ----- Identification (optional) -------------------------------------

    /** Visible tag / collar id distinct from the implanted microchip. */
    @Column(name = "identification_tag", length = 100)
    private String identificationTag;

    /** Kennel-club or registry number (pedigree registrations). */
    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    /** Municipal pet-license number where the owner's city requires one. */
    @Column(name = "city_license_number", length = 100)
    private String cityLicenseNumber;

    // ----- Behavior & lifestyle (optional) -------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", length = 20)
    private ActivityLevel activityLevel;

    /** Comma-separated temperament tags, validated/canonicalized in the service. */
    @Column(name = "temperament", length = 500)
    private String temperament;

    @Column(name = "social_behavior", length = 500)
    private String socialBehavior;

    /** Tri-state: null means "not specified yet". */
    @Column(name = "good_with_children")
    private Boolean goodWithChildren;

    /** Tri-state: null means "not specified yet". */
    @Column(name = "good_with_other_pets")
    private Boolean goodWithOtherPets;

    @Column(name = "behavioral_notes", length = 1000)
    private String behavioralNotes;

    // ----- Nutrition (optional) ------------------------------------------

    /** Free-text diet description, e.g. "grain-free", "vet renal diet". */
    @Column(name = "diet_type", length = 100)
    private String dietType;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_type", length = 20)
    private FoodType foodType;

    @Column(name = "food_brand", length = 100)
    private String foodBrand;

    @Column(name = "daily_meal_count")
    private Integer dailyMealCount;

    @Column(name = "daily_food_amount_grams")
    private Integer dailyFoodAmountGrams;

    @Column(name = "dietary_restrictions", length = 500)
    private String dietaryRestrictions;

    // ----- Ownership provenance (optional) -------------------------------

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "acquisition_method", length = 20)
    private AcquisitionMethod acquisitionMethod;

    @Column(name = "origin_description", length = 500)
    private String originDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PetStatus status = PetStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "deceased_at")
    private Instant deceasedAt;

    @Column(name = "lost_at")
    private Instant lostAt;

    @Column(name = "found_at")
    private Instant foundAt;

    @Column(name = "rehomed_at")
    private Instant rehomedAt;

    public static Pet create(UUID ownerId, String name, PetSpecies species) {
        return Pet.builder()
                .id(UUID.randomUUID())
                .ownerId(ownerId)
                .name(name)
                .species(species)
                .status(PetStatus.ACTIVE)
                .build();
    }

    public boolean belongsTo(UUID userId) {
        return ownerId.equals(userId);
    }

    public void archive() {
        requireTransitionAllowed();
        this.status = PetStatus.ARCHIVED;
        this.archivedAt = Instant.now();
    }

    public void restore() {
        if (status != PetStatus.ARCHIVED) {
            throw new IllegalStateException("Only archived pets can be restored");
        }
        this.status = PetStatus.ACTIVE;
        this.archivedAt = null;
    }

    public void markDeceased() {
        requireTransitionAllowed();
        this.status = PetStatus.DECEASED;
        this.deceasedAt = Instant.now();
        this.archivedAt = null;
    }

    /**
     * Profile marker only: does not touch Lost & Found advertisements, which
     * own a separate state machine. Allowed from ACTIVE or FOUND (a found
     * stray that goes missing again).
     */
    public void markLost() {
        requireTransitionAllowed();
        if (status != PetStatus.ACTIVE && status != PetStatus.FOUND) {
            throw new IllegalStateException("Only an active or found pet can be marked lost");
        }
        this.status = PetStatus.LOST;
        this.lostAt = Instant.now();
    }

    /** Allowed from LOST or FOUND (e.g. a shelter intake confirmed as a local pet). */
    public void markFound() {
        requireTransitionAllowed();
        if (status != PetStatus.LOST && status != PetStatus.FOUND) {
            throw new IllegalStateException("Only a lost or found pet can be marked found");
        }
        this.status = PetStatus.FOUND;
        this.foundAt = Instant.now();
    }

    /** Back to normal life after being reported lost/found. */
    public void markRecovered() {
        requireTransitionAllowed();
        if (status != PetStatus.LOST && status != PetStatus.FOUND) {
            throw new IllegalStateException("Only a lost or found pet can return to active");
        }
        this.status = PetStatus.ACTIVE;
    }

    /** Terminal: the profile becomes a permanent record and freezes. */
    public void markRehomed() {
        requireTransitionAllowed();
        this.status = PetStatus.REHOMED;
        this.rehomedAt = Instant.now();
    }

    /**
     * Editable while active/archived/lost/found (owners may keep completing a
     * profile), frozen once deceased or rehomed because those become permanent
     * records.
     */
    public void ensureEditable() {
        if (status.isTerminal()) {
            throw new IllegalStateException(
                    "Pet is " + status.name().toLowerCase() + " and its profile can no longer change");
        }
    }

    private void requireTransitionAllowed() {
        if (status.isTerminal()) {
            throw new IllegalStateException("Pet is in a terminal status and its state can no longer change");
        }
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
