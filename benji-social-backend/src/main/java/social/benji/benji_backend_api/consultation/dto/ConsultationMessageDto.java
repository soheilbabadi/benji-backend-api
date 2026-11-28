package social.benji.benji_backend_api.consultation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationMessageDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    private String consultationId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String senderId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String senderRole;
    private String content;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean systemMessage;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;
}
