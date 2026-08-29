package social.benji.benji_backend_api.consultation.domain.model;

import lombok.Getter;
import social.benji.benji_backend_api.consultation.domain.valueobject.PetDataType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a snapshot of pet data shared with an expert for a specific consultation.
 * This is a separate aggregate to maintain explicit consent boundaries.
 */
@Getter
public class ConsultationSharedPetData {

    private final UUID id;
    private final UUID consultationId;
    private final PetDataType dataType;
    private final String contentSnapshot; // JSON representation of the data at time of sharing
    private final Instant createdAt;

    protected ConsultationSharedPetData() {
        // For JPA
        this.id = null;
        this.consultationId = null;
        this.dataType = null;
        this.contentSnapshot = null;
        this.createdAt = null;
    }

    public ConsultationSharedPetData(UUID consultationId, PetDataType dataType, String contentSnapshot) {
        this.id = UUID.randomUUID();
        this.consultationId = Objects.requireNonNull(consultationId, "Consultation ID cannot be null");
        this.dataType = Objects.requireNonNull(dataType, "Pet data type cannot be null");
        this.contentSnapshot = Objects.requireNonNull(contentSnapshot, "Content snapshot cannot be null");
        this.createdAt = Instant.now();
    }
}
