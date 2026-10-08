package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents a message in a consultation conversation.
 * Messages are immutable after creation.
 */
@Document(collection = "consultation_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationMessage {

    @Id
    private String id;
    private String consultationId;
    private String senderId;
    private String senderRole; // 'OWNER', 'EXPERT', 'SYSTEM'
    private String content;
    private boolean isSystemMessage;
    private Instant createdAt;
}
