package social.benji.benji_backend_api.payment.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.payment.domain.model.PaymentEventOutbox;

import java.util.List;
import java.util.UUID;

public interface PaymentEventOutboxRepository extends JpaRepository<PaymentEventOutbox, UUID> {
    List<PaymentEventOutbox> findTop50ByProcessedFalseOrderByOccurredAtAsc();
}
