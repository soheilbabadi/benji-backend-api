package social.benji.benji_backend_api.consultation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationSharedPetData;
import social.benji.benji_backend_api.consultation.domain.valueobject.PetDataType;

import java.util.List;
import java.util.Optional;

public interface ConsultationSharedPetDataRepository extends MongoRepository<ConsultationSharedPetData, String> {

    List<ConsultationSharedPetData> findByConsultationId(String consultationId);

    Optional<ConsultationSharedPetData> findByConsultationIdAndDataType(String consultationId, PetDataType dataType);
}
