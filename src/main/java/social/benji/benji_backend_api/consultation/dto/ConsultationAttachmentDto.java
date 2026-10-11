package social.benji.benji_backend_api.consultation.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationAttachmentDto {

    private String id;
    private String consultationId;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private String storageKey;
    private String uploadedBy;
    private Instant createdAt;
}
