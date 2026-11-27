package social.benji.benji_backend_api.lostfound.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.lostfound.domain.PaymentSucceededProcessingEntity;

import java.util.UUID;

public interface PaymentSucceededProcessingRepository
        extends JpaRepository<PaymentSucceededProcessingEntity, UUID> {
    void deleteByAdvertisementPublicId(UUID advertisementPublicId);
}
