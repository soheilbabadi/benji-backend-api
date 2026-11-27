package social.benji.benji_backend_api.lostfound.domain.repository;

import social.benji.benji_backend_api.lostfound.domain.model.LostAdvertisement;

import java.util.Optional;
import java.util.UUID;

public interface LostAdvertisementRepository {
    LostAdvertisement save(LostAdvertisement advertisement);
    Optional<LostAdvertisement> findById(UUID id);
    boolean existsById(UUID id);
    void deleteById(UUID id);
}
