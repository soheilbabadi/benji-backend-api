package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents an attachment (file) uploaded to a consultation.
 * Files are stored externally; this entity holds metadata only.
 */
@Document(collection = "consultation_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationAttachment {

    @Id
    private String id;
    private String consultationId;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private String storageKey; // Path/key in external storage
    private String uploadedBy;
    private Instant createdAt;
}
