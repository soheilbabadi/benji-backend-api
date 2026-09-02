package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a message in a consultation conversation.
 * Messages are immutable after creation.
 */
@Entity
@Table(name = "consultation_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationMessage {

    @Id
    private UUID id;
    private UUID consultationId;
    private UUID senderId;
    private String senderRole; // 'OWNER', 'EXPERT', 'SYSTEM'
    private String content;
    private boolean isSystemMessage;
    private Instant createdAt;
}
