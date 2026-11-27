package social.benji.benji_backend_api.consultation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationAnswer;

import java.util.Optional;
import java.util.List;

public interface ConsultationAnswerRepository extends MongoRepository<ConsultationAnswer, String> {

    Optional<ConsultationAnswer> findByExpertId(String expertId);
    Optional<ConsultationAnswer> findByConsultationId(String consultationId);
    List<ConsultationAnswer> findAllByConsultationId(String consultationId);
}
