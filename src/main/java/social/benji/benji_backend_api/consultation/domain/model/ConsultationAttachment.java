package social.benji.benji_backend_api.consultation.domain.model;

import lombok.Getter;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an attachment (file) uploaded to a consultation.
 * Files are stored externally; this entity holds metadata only.
 */
@Getter
public class ConsultationAttachment {

    private final UUID id;
    private final UUID consultationId;
    private final String originalFilename;
    private final String contentType;
    private final long fileSize;
    private final String storageKey; // Path/key in external storage
    private final UUID uploadedBy;
    private final Instant createdAt;

    protected ConsultationAttachment() {
        // For JPA
        this.id = null;
        this.consultationId = null;
        this.originalFilename = null;
        this.contentType = null;
        this.fileSize = 0;
        this.storageKey = null;
        this.uploadedBy = null;
        this.createdAt = null;
    }

    public ConsultationAttachment(UUID consultationId, String originalFilename, String contentType,
                                  long fileSize, String storageKey, UUID uploadedBy) {
        this.id = UUID.randomUUID();
        this.consultationId = Objects.requireNonNull(consultationId, "Consultation ID cannot be null");
        this.originalFilename = Objects.requireNonNull(originalFilename, "Original filename cannot be null");
        this.contentType = Objects.requireNonNull(contentType, "Content type cannot be null");
        if (fileSize <= 0) {
            throw new IllegalArgumentException("File size must be positive");
        }
        this.fileSize = fileSize;
        this.storageKey = Objects.requireNonNull(storageKey, "Storage key cannot be null");
        this.uploadedBy = Objects.requireNonNull(uploadedBy, "Uploaded by cannot be null");
        this.createdAt = Instant.now();
    }
}
