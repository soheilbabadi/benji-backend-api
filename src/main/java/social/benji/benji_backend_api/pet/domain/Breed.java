package social.benji.benji_backend_api.pet.domain;

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
 * Lookup row for dog/cat breeds. MIXED and UNKNOWN are seeded special rows so
 * the pet form always has an honest option without nullable-column semantics.
 */
@Entity
@Table(name = "tbl_breeds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Breed {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "species", nullable = false, length = 20)
    private PetSpecies species;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_mixed", nullable = false)
    @Builder.Default
    private boolean mixed = false;

    @Column(name = "is_unknown", nullable = false)
    @Builder.Default
    private boolean unknown = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }
}
