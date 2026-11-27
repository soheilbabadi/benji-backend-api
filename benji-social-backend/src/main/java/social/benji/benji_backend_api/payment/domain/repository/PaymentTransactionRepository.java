package social.benji.benji_backend_api.payment.domain.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import social.benji.benji_backend_api.payment.domain.model.PaymentTransaction;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByGatewayAuthority(String gatewayAuthority);

    Optional<PaymentTransaction> findTopByServiceTypeAndServiceReferenceIdOrderByCreatedAtDesc(
            PaidServiceType serviceType, UUID serviceReferenceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentTransaction p where p.id = :id")
    Optional<PaymentTransaction> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentTransaction p where p.gatewayAuthority = :authority")
    Optional<PaymentTransaction> findByGatewayAuthorityForUpdate(@Param("authority") String authority);
}
