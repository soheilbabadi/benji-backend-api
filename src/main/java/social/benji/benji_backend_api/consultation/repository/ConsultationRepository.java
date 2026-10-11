package social.benji.benji_backend_api.consultation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import social.benji.benji_backend_api.consultation.domain.model.Consultation;
import social.benji.benji_backend_api.consultation.domain.valueobject.ConsultationStatus;

import java.util.List;

public interface ConsultationRepository extends MongoRepository<Consultation, String> {

    List<Consultation> findByOwnerId(String ownerId);

    List<Consultation> findByAssignedExpertId(String expertId);

    List<Consultation> findByStatus(ConsultationStatus status);

    List<Consultation> findByOwnerIdAndStatus(String ownerId, ConsultationStatus status);
}
