package social.benji.benji_backend_api.payment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import social.benji.benji_backend_api.payment.domain.PaymentStatus;
import social.benji.benji_backend_api.payment.domain.PaymentTransactionEntity;
import social.benji.benji_backend_api.payment.domain.ServiceType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, UUID> {

    Optional<PaymentTransactionEntity> findByPublicToken(UUID publicToken);

    /** Gateway-side anchor: ZarinPal callbacks carry Authority; unique index guarantees ≤1 row. */
    Optional<PaymentTransactionEntity> findByGatewayAuthority(String gatewayAuthority);

    /** The single in-flight attempt for a business operation (partial unique index uk_payment_one_active_attempt_per_op). */
    Optional<PaymentTransactionEntity> findFirstByServiceTypeAndServiceReferenceIdAndStatusIn(
            ServiceType serviceType, UUID serviceReferenceId, List<PaymentStatus> inFlight);

    boolean existsByServiceTypeAndServiceReferenceIdAndStatusIn(
            ServiceType serviceType, UUID serviceReferenceId, List<PaymentStatus> statuses);

    Page<PaymentTransactionEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Pessimistic lock used by the verification path so duplicate/concurrent callbacks serialize:
     * the second transaction blocks until the first commits, then observes SUCCESS and no-ops.
     */
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PaymentTransactionEntity t where t.gatewayAuthority = :authority")
    Optional<PaymentTransactionEntity> findByGatewayAuthorityForUpdate(@Param("authority") String authority);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PaymentTransactionEntity t where t.publicToken = :token")
    Optional<PaymentTransactionEntity> findByPublicTokenForUpdate(@Param("token") UUID token);

    /** Reconciliation candidates: in-flight rows older than the cutoff. */
    @Query("""
           select t from PaymentTransactionEntity t
            where t.status in (social.benji.benji_backend_api.payment.domain.PaymentStatus.PENDING,
                               social.benji.benji_backend_api.payment.domain.PaymentStatus.UNKNOWN)
              and t.updatedAt < :cutoff
            order by t.updatedAt asc
           """)
    List<PaymentTransactionEntity> findStaleInFlight(@Param("cutoff") Instant cutoff, Pageable pageable);
}
