package social.benji.benji_backend_api.consultation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationAnswer;

import java.util.Optional;

public interface ConsultationAnswerRepository extends MongoRepository<ConsultationAnswer, String> {

    Optional<ConsultationAnswer> findByExpertId(String expertId);
}
