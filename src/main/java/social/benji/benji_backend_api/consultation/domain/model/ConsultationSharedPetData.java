package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.PetDataType;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a snapshot of pet data shared with an expert for a specific consultation.
 * This is a separate aggregate to maintain explicit consent boundaries.
 */
@Entity
@Table(name = "consultation_shared_pet_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationSharedPetData {

    @Id
    private UUID id;
    private UUID consultationId;
    @Enumerated(EnumType.STRING)
    private PetDataType dataType;
    private String contentSnapshot; // JSON representation of the data at time of sharing
    private Instant createdAt;
}
