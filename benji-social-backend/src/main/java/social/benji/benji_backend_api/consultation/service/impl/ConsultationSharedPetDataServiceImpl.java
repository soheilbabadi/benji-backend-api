package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.domain.valueobject.PetDataType;
import social.benji.benji_backend_api.consultation.dto.ConsultationSharedPetDataDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationSharedPetDataMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationSharedPetDataRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationSharedPetDataService;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.consultation.security.ConsultationAuthorizationService;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationSharedPetData;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationSharedPetDataServiceImpl implements ConsultationSharedPetDataService {

    private final ConsultationSharedPetDataRepository consultationSharedPetDataRepository;
    private final ConsultationSharedPetDataMapper consultationSharedPetDataMapper;
    private final ConsultationAuthorizationService authorization;

    @Override
    public ConsultationSharedPetDataDto create(ConsultationSharedPetDataDto sharedPetDataDto) {
        if (sharedPetDataDto == null || sharedPetDataDto.getConsultationId() == null
                || sharedPetDataDto.getConsultationId().isBlank()) {
            throw new BenjiCustomException.ValidationException("Consultation ID is required");
        }
        authorization.requireOwner(sharedPetDataDto.getConsultationId());
        ConsultationSharedPetData data = consultationSharedPetDataMapper.toModel(sharedPetDataDto);
        data.setCreatedAt(java.time.Instant.now());
        return consultationSharedPetDataMapper.toDto(
                consultationSharedPetDataRepository.save(data));
    }

    @Override
    public ConsultationSharedPetDataDto findById(String id) {
        ConsultationSharedPetData data = consultationSharedPetDataRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationSharedPetData not found"));
        authorization.requireParticipant(data.getConsultationId());
        return consultationSharedPetDataMapper.toDto(data);
    }

    @Override
    public List<ConsultationSharedPetDataDto> findAll() {
        authorization.requireAdmin();
        return consultationSharedPetDataRepository.findAll().stream()
                .map(consultationSharedPetDataMapper::toDto)
                .toList();
    }

    @Override
    public List<ConsultationSharedPetDataDto> findByConsultationId(String consultationId) {
        authorization.requireParticipant(consultationId);
        return consultationSharedPetDataRepository.findByConsultationId(consultationId).stream().map(consultationSharedPetDataMapper::toDto).toList();
    }

    @Override
    public ConsultationSharedPetDataDto findByConsultationIdAndDataType(String consultationId, PetDataType dataType) {
        authorization.requireParticipant(consultationId);
        return consultationSharedPetDataRepository.findByConsultationIdAndDataType(consultationId, dataType)
                .map(consultationSharedPetDataMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationSharedPetData not found for consultationId: " + consultationId + " and dataType: " + dataType));
    }

    @Override
    public ConsultationSharedPetDataDto update(ConsultationSharedPetDataDto sharedPetDataDto) {
        if (sharedPetDataDto == null || sharedPetDataDto.getId() == null) {
            throw new BenjiCustomException.ValidationException("Shared pet data ID is required");
        }
        ConsultationSharedPetData existing = consultationSharedPetDataRepository.findById(sharedPetDataDto.getId())
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationSharedPetData not found"));
        authorization.requireOwner(existing.getConsultationId());
        if (sharedPetDataDto.getDataType() != null) existing.setDataType(sharedPetDataDto.getDataType());
        if (sharedPetDataDto.getContentSnapshot() != null) existing.setContentSnapshot(sharedPetDataDto.getContentSnapshot());
        return consultationSharedPetDataMapper.toDto(consultationSharedPetDataRepository.save(existing));
    }

    @Override
    public void deleteById(String id) {
        ConsultationSharedPetData existing = consultationSharedPetDataRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationSharedPetData not found"));
        authorization.requireOwner(existing.getConsultationId());
        consultationSharedPetDataRepository.deleteById(id);
    }
}
