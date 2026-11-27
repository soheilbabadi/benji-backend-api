package social.benji.benji_backend_api.consultation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationAttachment;

import java.util.List;

public interface ConsultationAttachmentRepository extends MongoRepository<ConsultationAttachment, String> {

    List<ConsultationAttachment> findByConsultationId(String consultationId);

    List<ConsultationAttachment> findByUploadedBy(String uploadedBy);
}
