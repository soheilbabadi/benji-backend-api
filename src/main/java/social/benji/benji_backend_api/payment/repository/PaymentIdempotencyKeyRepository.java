package social.benji.benji_backend_api.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.payment.domain.PaymentIdempotencyKeyEntity;

import java.time.Instant;
import java.util.List;

public interface PaymentIdempotencyKeyRepository extends JpaRepository<PaymentIdempotencyKeyEntity, String> {

    List<PaymentIdempotencyKeyEntity> findAllByExpiresAtBefore(Instant cutoff);
}
