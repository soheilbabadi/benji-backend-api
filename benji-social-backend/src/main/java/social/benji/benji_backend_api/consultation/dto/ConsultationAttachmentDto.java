package social.benji.benji_backend_api.consultation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationAttachmentDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    private String consultationId;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String storageKey;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String uploadedBy;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;
}
