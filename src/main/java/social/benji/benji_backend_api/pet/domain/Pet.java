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
@Table(name = "pets")
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

    @Column(name = "color", length = 50)
    private String color;

    /** Profile snapshot only; historical measurements belong to Weight & Growth. */
    @Column(name = "current_weight_kg", precision = 6, scale = 2)
    private BigDecimal currentWeightKg;

    /** Optional; duplicates across users are tolerated (no unique constraint). */
    @Column(name = "microchip_number", length = 50)
    private String microchipNumber;

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
     * Editable while active or archived (owners may complete an archived pet's
     * profile), frozen once deceased because it becomes part of the permanent
     * life record.
     */
    public void ensureEditable() {
        requireTransitionAllowed();
    }

    private void requireTransitionAllowed() {
        if (status.isTerminal()) {
            throw new IllegalStateException("Pet is deceased and its state can no longer change");
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
