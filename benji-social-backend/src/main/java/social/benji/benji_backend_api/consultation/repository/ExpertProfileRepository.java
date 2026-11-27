package social.benji.benji_backend_api.consultation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import social.benji.benji_backend_api.consultation.domain.model.ExpertProfile;
import social.benji.benji_backend_api.consultation.domain.valueobject.ExpertVerificationStatus;

import java.util.List;
import java.util.Optional;

public interface ExpertProfileRepository extends MongoRepository<ExpertProfile, String> {

    Optional<ExpertProfile> findByUserId(String userId);

    List<ExpertProfile> findByVerificationStatus(ExpertVerificationStatus verificationStatus);

    List<ExpertProfile> findByIsActiveTrue();

    List<ExpertProfile> findBySpecialtiesContaining(String specialty);
}
