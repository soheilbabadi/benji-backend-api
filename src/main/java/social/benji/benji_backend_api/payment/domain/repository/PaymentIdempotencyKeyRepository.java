package social.benji.benji_backend_api.payment.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.payment.domain.model.PaymentIdempotencyKey;

public interface PaymentIdempotencyKeyRepository
        extends JpaRepository<PaymentIdempotencyKey, String> {
}
