package social.benji.benji_backend_api.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Transactional outbox row (approved decision E: outbox retained).
 *
 * <p>Written in the SAME database transaction that settles a payment outcome, guaranteeing the
 * event survives even if the process crashes before publication. The relay polls unprocessed
 * rows and republishes them as Spring application events — at-least-once delivery; consumers
 * must be idempotent.</p>
 */
@Entity
@Table(name = "payment_event_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEventOutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private PaymentEventType eventType;

    /** payment_transaction.id that produced this event. */
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    @Builder.Default
    private String aggregateType = "PaymentTransaction";

    /** JSON payload of the domain event (see PaymentSucceededPayload etc.). */
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    @Builder.Default
    private Instant occurredAt = Instant.now();

    @Column(name = "processed", nullable = false)
    @Builder.Default
    private boolean processed = false;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @Column(name = "last_error")
    private String lastError;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public void markProcessed() {
        this.processed = true;
        this.processedAt = Instant.now();
    }

    public void markFailure(String error) {
        this.retryCount++;
        this.lastError = error != null && error.length() > 500 ? error.substring(0, 500) : error;
    }
}
