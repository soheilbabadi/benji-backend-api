package social.benji.benji_backend_api.consultation.domain.model;

import lombok.Getter;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a message in a consultation conversation.
 * Messages are immutable after creation.
 */
@Getter
public class ConsultationMessage {

    private final UUID id;
    private final UUID consultationId;
    private final UUID senderId;
    private final String senderRole; // 'OWNER', 'EXPERT', 'SYSTEM'
    private final String content;
    private final boolean isSystemMessage;
    private final Instant createdAt;

    protected ConsultationMessage() {
        // For JPA
        this.id = null;
        this.consultationId = null;
        this.senderId = null;
        this.senderRole = null;
        this.content = null;
        this.isSystemMessage = false;
        this.createdAt = null;
    }

    public ConsultationMessage(UUID consultationId, UUID senderId, String senderRole, 
                               String content, boolean isSystemMessage) {
        this.id = UUID.randomUUID();
        this.consultationId = Objects.requireNonNull(consultationId, "Consultation ID cannot be null");
        this.senderId = Objects.requireNonNull(senderId, "Sender ID cannot be null");
        this.senderRole = Objects.requireNonNull(senderRole, "Sender role cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        if (content.isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        this.isSystemMessage = isSystemMessage;
        this.createdAt = Instant.now();
    }

    public static ConsultationMessage createSystemMessage(UUID consultationId, String content) {
        return new ConsultationMessage(consultationId, UUID.randomUUID(), "SYSTEM", content, true);
    }
}
