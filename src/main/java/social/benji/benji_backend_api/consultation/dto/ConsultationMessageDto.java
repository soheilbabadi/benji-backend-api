package social.benji.benji_backend_api.consultation.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationMessageDto {

    private String id;
    private String consultationId;
    private String senderId;
    private String senderRole;
    private String content;
    private boolean systemMessage;
    private Instant createdAt;
}
