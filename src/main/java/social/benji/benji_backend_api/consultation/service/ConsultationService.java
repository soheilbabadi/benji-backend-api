package social.benji.benji_backend_api.consultation.service;

import social.benji.benji_backend_api.consultation.domain.valueobject.ConsultationStatus;
import social.benji.benji_backend_api.consultation.dto.ConsultationDto;

import java.util.List;

public interface ConsultationService {

    ConsultationDto create(ConsultationDto consultationDto);

    ConsultationDto findById(String id);

    List<ConsultationDto> findAll();

    List<ConsultationDto> findByOwnerId(String ownerId);

    List<ConsultationDto> findByAssignedExpertId(String expertId);

    List<ConsultationDto> findByStatus(ConsultationStatus status);

    List<ConsultationDto> findByOwnerIdAndStatus(String ownerId, ConsultationStatus status);

    ConsultationDto update(ConsultationDto consultationDto);

    void deleteById(String id);
}
