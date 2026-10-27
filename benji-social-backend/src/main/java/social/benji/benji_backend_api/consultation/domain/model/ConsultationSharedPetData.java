package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import social.benji.benji_backend_api.consultation.domain.valueobject.PetDataType;

import java.time.Instant;

/**
 * Represents a snapshot of pet data shared with an expert for a specific consultation.
 * This is a separate aggregate to maintain explicit consent boundaries.
 */
@Document(collection = "consultation_shared_pet_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationSharedPetData {

    @Id
    private String id;
    private String consultationId;
    private PetDataType dataType;
    private String contentSnapshot; // JSON representation of the data at time of sharing
    private Instant createdAt;
}
