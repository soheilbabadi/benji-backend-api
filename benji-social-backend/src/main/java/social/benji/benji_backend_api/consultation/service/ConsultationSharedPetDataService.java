package social.benji.benji_backend_api.consultation.service;

import social.benji.benji_backend_api.consultation.domain.valueobject.PetDataType;
import social.benji.benji_backend_api.consultation.dto.ConsultationSharedPetDataDto;

import java.util.List;

public interface ConsultationSharedPetDataService {

    ConsultationSharedPetDataDto create(ConsultationSharedPetDataDto sharedPetDataDto);

    ConsultationSharedPetDataDto findById(String id);

    List<ConsultationSharedPetDataDto> findAll();

    List<ConsultationSharedPetDataDto> findByConsultationId(String consultationId);

    ConsultationSharedPetDataDto findByConsultationIdAndDataType(String consultationId, PetDataType dataType);

    ConsultationSharedPetDataDto update(ConsultationSharedPetDataDto sharedPetDataDto);

    void deleteById(String id);
}
