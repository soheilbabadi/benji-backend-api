package social.benji.benji_backend_api.lostfound.domain.repository;

import social.benji.benji_backend_api.lostfound.domain.model.FoundAdvertisement;

import java.util.Optional;
import java.util.UUID;

public interface FoundAdvertisementRepository {
    FoundAdvertisement save(FoundAdvertisement advertisement);
    Optional<FoundAdvertisement> findById(UUID id);
    boolean existsById(UUID id);
    void deleteById(UUID id);
}
