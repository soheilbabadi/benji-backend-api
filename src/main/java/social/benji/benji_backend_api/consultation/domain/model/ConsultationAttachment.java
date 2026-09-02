package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents an attachment (file) uploaded to a consultation.
 * Files are stored externally; this entity holds metadata only.
 */
@Entity
@Table(name = "consultation_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationAttachment {

    @Id
    private UUID id;
    private UUID consultationId;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private String storageKey; // Path/key in external storage
    private UUID uploadedBy;
    private Instant createdAt;
}
