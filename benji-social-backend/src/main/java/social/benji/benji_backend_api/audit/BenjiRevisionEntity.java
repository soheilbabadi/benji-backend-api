package social.benji.benji_backend_api.audit;

import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@RevisionEntity(BenjiRevisionListener.class)
@Table(name = "benji_revinfo")
public class BenjiRevisionEntity {
    @Id
    @GeneratedValue
    @RevisionNumber
    @Column(name = "revision_id")
    private int revisionId;

    @RevisionTimestamp
    @Column(name = "revision_timestamp", nullable = false)
    private Instant revisionTimestamp;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20)
    private BenjiActorType actorType = BenjiActorType.SYSTEM;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    public int getRevisionId() { return revisionId; }
    public Instant getRevisionTimestamp() { return revisionTimestamp; }
    public UUID getActorUserId() { return actorUserId; }
    public BenjiActorType getActorType() { return actorType; }
    public String getCorrelationId() { return correlationId; }
    public void setRevisionTimestamp(Instant revisionTimestamp) { this.revisionTimestamp = revisionTimestamp; }
    public void setActorUserId(UUID actorUserId) { this.actorUserId = actorUserId; }
    public void setActorType(BenjiActorType actorType) { this.actorType = actorType; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
