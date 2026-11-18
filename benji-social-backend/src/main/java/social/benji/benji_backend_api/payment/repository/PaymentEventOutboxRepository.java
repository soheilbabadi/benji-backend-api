package social.benji.benji_backend_api.payment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.payment.domain.PaymentEventOutboxEntity;

import java.util.List;
import java.util.UUID;

public interface PaymentEventOutboxRepository extends JpaRepository<PaymentEventOutboxEntity, UUID> {

    /** Relay polling query (idx_outbox_processed_occurred). */
    List<PaymentEventOutboxEntity> findAllByProcessedFalseOrderByOccurredAtAsc(Pageable pageable);

    boolean existsByAggregateIdAndEventType(UUID aggregateId,
                                            social.benji.benji_backend_api.payment.domain.PaymentEventType type);
}
