package social.benji.benji_backend_api.payment.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.payment.domain.model.Tariff;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

import java.util.Optional;
import java.util.UUID;

public interface TariffRepository extends JpaRepository<Tariff, UUID> {
    Optional<Tariff> findByServiceTypeAndActiveTrue(PaidServiceType serviceType);
}
