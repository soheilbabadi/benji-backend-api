package social.benji.benji_backend_api.consultation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationMessage;

import java.util.List;

public interface ConsultationMessageRepository extends MongoRepository<ConsultationMessage, String> {

    List<ConsultationMessage> findByConsultationIdOrderByCreatedAtAsc(String consultationId);

    List<ConsultationMessage> findByConsultationIdAndSenderId(String consultationId, String senderId);
}
