package social.benji.benji_backend_api.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.payment.domain.ServiceType;
import social.benji.benji_backend_api.payment.domain.TariffEntity;

import java.util.Optional;
import java.util.UUID;

public interface TariffRepository extends JpaRepository<TariffEntity, UUID> {

    /**
     * The DB partial unique index uk_tariff_one_active_per_service guarantees at most one hit;
     * ordering by activated_at desc is a defensive tie-breaker only.
     */
    Optional<TariffEntity> findFirstByServiceTypeAndActiveTrueOrderByActivatedAtDesc(ServiceType serviceType);
}
