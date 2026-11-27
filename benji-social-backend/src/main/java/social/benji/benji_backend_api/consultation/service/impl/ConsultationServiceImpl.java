package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.domain.valueobject.ConsultationStatus;
import social.benji.benji_backend_api.consultation.dto.ConsultationDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationService;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.consultation.security.ConsultationAuthorizationService;
import social.benji.benji_backend_api.consultation.domain.model.Consultation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final ConsultationMapper consultationMapper;
    private final ConsultationAuthorizationService authorization;

    @Override
    public ConsultationDto create(ConsultationDto consultationDto) {
        if (consultationDto == null) {
            throw new BenjiCustomException.ValidationException("Consultation request is required");
        }
        String callerId = authorization.callerId();
        Consultation consultation = consultationMapper.toModel(consultationDto);
        consultation.setOwnerId(callerId);
        consultation.setAssignedExpertId(null);
        consultation.setStatus(social.benji.benji_backend_api.consultation.domain.valueobject.ConsultationStatus.DRAFT);
        java.time.Instant now = java.time.Instant.now();
        consultation.setCreatedAt(now);
        consultation.setUpdatedAt(now);
        consultation.setClosedAt(null);
        consultation.setAnsweredAt(null);
        consultation.setAnswer(null);
        consultation.setAttachmentIds(null);
        consultation.setSharedPetDataTypes(null);
        return consultationMapper.toDto(consultationRepository.save(consultation));
    }

    @Override
    public ConsultationDto findById(String id) {
        return consultationMapper.toDto(authorization.requireParticipant(id));
    }

    @Override
    public List<ConsultationDto> findAll() {
        if (authorization.isAdmin()) {
            return consultationRepository.findAll().stream().map(consultationMapper::toDto).toList();
        }
        String callerId = authorization.callerId();
        return consultationRepository.findAll().stream()
                .filter(cons -> callerId.equals(cons.getOwnerId())
                        || callerId.equals(cons.getAssignedExpertId()))
                .map(consultationMapper::toDto)
                .toList();
    }

    @Override
    public List<ConsultationDto> findByOwnerId(String ownerId) {
        authorization.requireOwnerOrAdminForUserId(ownerId);
        return consultationRepository.findByOwnerId(ownerId).stream().map(consultationMapper::toDto).toList();
    }

    @Override
    public List<ConsultationDto> findByAssignedExpertId(String expertId) {
        authorization.requireOwnerOrAdminForUserId(expertId);
        return consultationRepository.findByAssignedExpertId(expertId).stream().map(consultationMapper::toDto).toList();
    }

    @Override
    public List<ConsultationDto> findByStatus(ConsultationStatus status) {
        if (authorization.isAdmin()) {
            return consultationRepository.findByStatus(status).stream().map(consultationMapper::toDto).toList();
        }
        String callerId = authorization.callerId();
        return consultationRepository.findByStatus(status).stream()
                .filter(cons -> callerId.equals(cons.getOwnerId())
                        || callerId.equals(cons.getAssignedExpertId()))
                .map(consultationMapper::toDto)
                .toList();
    }

    @Override
    public List<ConsultationDto> findByOwnerIdAndStatus(String ownerId, ConsultationStatus status) {
        authorization.requireOwnerOrAdminForUserId(ownerId);
        return consultationRepository.findByOwnerIdAndStatus(ownerId, status).stream().map(consultationMapper::toDto).toList();
    }

    @Override
    public ConsultationDto update(ConsultationDto consultationDto) {
        authorization.requireAdmin();
        if (consultationDto == null || consultationDto.getId() == null) {
            throw new BenjiCustomException.ValidationException("Consultation ID is required");
        }
        Consultation existing = authorization.requireConsultation(consultationDto.getId());
        if (consultationDto.getPetId() != null) existing.setPetId(consultationDto.getPetId());
        if (consultationDto.getCategory() != null) existing.setCategory(consultationDto.getCategory());
        if (consultationDto.getSubject() != null) existing.setSubject(consultationDto.getSubject());
        if (consultationDto.getQuestion() != null) existing.setQuestion(consultationDto.getQuestion());
        if (consultationDto.getPrice() != null) existing.setPrice(consultationDto.getPrice());
        existing.setEmergencyDisclaimerAccepted(consultationDto.isEmergencyDisclaimerAccepted());
        existing.setUpdatedAt(java.time.Instant.now());
        return consultationMapper.toDto(consultationRepository.save(existing));
    }

    @Override
    public void deleteById(String id) {
        authorization.requireAdmin();
        if (!consultationRepository.existsById(id)) {
            throw new BenjiCustomException.ResourceNotFoundException("Consultation not found with id: " + id);
        }
        consultationRepository.deleteById(id);
    }
}
