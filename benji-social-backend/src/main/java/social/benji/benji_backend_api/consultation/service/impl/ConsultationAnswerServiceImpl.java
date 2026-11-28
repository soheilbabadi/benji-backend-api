package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.dto.ConsultationAnswerDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationAnswerMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationAnswerRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationAnswerService;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.consultation.security.ConsultationAuthorizationService;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationAnswer;
import social.benji.benji_backend_api.consultation.repository.ExpertProfileRepository;
import social.benji.benji_backend_api.consultation.repository.ConsultationRepository;
import social.benji.benji_backend_api.consultation.domain.valueobject.ExpertVerificationStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationAnswerServiceImpl implements ConsultationAnswerService {

    private final ConsultationAnswerRepository consultationAnswerRepository;
    private final ConsultationAnswerMapper consultationAnswerMapper;
    private final ConsultationAuthorizationService authorization;
    private final ExpertProfileRepository expertProfileRepository;
    private final ConsultationRepository consultationRepository;

    @Override
    public ConsultationAnswerDto create(ConsultationAnswerDto answerDto) {
        if (answerDto == null || answerDto.getConsultationId() == null
                || answerDto.getConsultationId().isBlank()) {
            throw new BenjiCustomException.ValidationException("Consultation ID is required");
        }
        String callerId = authorization.callerId();
        social.benji.benji_backend_api.consultation.domain.model.Consultation consultation =
                authorization.requireAssignedExpert(answerDto.getConsultationId());
        if (!callerId.equals(consultation.getAssignedExpertId())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the assigned expert can submit an answer");
        }
        expertProfileRepository.findByUserId(callerId)
                .filter(profile -> profile.getVerificationStatus() == ExpertVerificationStatus.VERIFIED
                        && profile.isActive())
                .orElseThrow(() -> new BenjiCustomException.ResourceForbiddenException(
                        "A verified expert profile is required"));
        ConsultationAnswer answer = consultationAnswerMapper.toModel(answerDto);
        answer.setConsultationId(answerDto.getConsultationId());
        answer.setExpertId(callerId);
        answer.setSubmittedAt(java.time.Instant.now());
        ConsultationAnswer saved = consultationAnswerRepository.save(answer);
        consultation.setAnswer(saved);
        consultationRepository.save(consultation);
        return consultationAnswerMapper.toDto(saved);
    }

    @Override
    public ConsultationAnswerDto findById(String id) {
        ConsultationAnswer answer = consultationAnswerRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationAnswer not found"));
        authorization.requireParticipant(answer.getConsultationId());
        return consultationAnswerMapper.toDto(answer);
    }

    @Override
    public List<ConsultationAnswerDto> findAll() {
        return authorization.accessibleConsultations().stream()
                .flatMap(consultation -> consultationAnswerRepository
                        .findAllByConsultationId(consultation.getId()).stream())
                .map(consultationAnswerMapper::toDto)
                .toList();
    }

    @Override
    public ConsultationAnswerDto findByExpertId(String expertId) {
        if (!authorization.isAdmin()) {
            if (!authorization.callerId().equals(expertId)) {
                throw new BenjiCustomException.ResourceForbiddenException(
                        "Only the expert or an administrator can query these answers");
            }
            expertProfileRepository.findByUserId(expertId)
                    .orElseThrow(() -> new BenjiCustomException.ResourceForbiddenException(
                            "An expert profile is required"));
        }
        java.util.Optional<ConsultationAnswer> answer =
                consultationAnswerRepository.findByExpertId(expertId);
        if (answer.isEmpty()) {
            throw new BenjiCustomException.ResourceNotFoundException(
                    "ConsultationAnswer not found for expertId: " + expertId);
        }
        ConsultationAnswer result = answer.get();
        authorization.requireParticipant(result.getConsultationId());
        return consultationAnswerMapper.toDto(result);
    }

    @Override
    public ConsultationAnswerDto update(ConsultationAnswerDto answerDto) {
        if (answerDto == null || answerDto.getId() == null) {
            throw new BenjiCustomException.ValidationException("Answer ID is required");
        }
        ConsultationAnswer existing = consultationAnswerRepository.findById(answerDto.getId())
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationAnswer not found"));
        authorization.requireParticipant(existing.getConsultationId());
        if (!authorization.isAdmin() && !authorization.callerId().equals(existing.getExpertId())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the assigned expert or an administrator can update this answer");
        }
        if (answerDto.getAssessment() != null) existing.setAssessment(answerDto.getAssessment());
        if (answerDto.getRecommendedActions() != null) existing.setRecommendedActions(answerDto.getRecommendedActions());
        if (answerDto.getWarningSigns() != null) existing.setWarningSigns(answerDto.getWarningSigns());
        existing.setInPersonVisitRecommended(answerDto.isInPersonVisitRecommended());
        if (answerDto.getUrgency() != null) existing.setUrgency(answerDto.getUrgency());
        existing.setSubmittedAt(java.time.Instant.now());
        return consultationAnswerMapper.toDto(consultationAnswerRepository.save(existing));
    }

    @Override
    public void deleteById(String id) {
        ConsultationAnswer existing = consultationAnswerRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationAnswer not found"));
        authorization.requireParticipant(existing.getConsultationId());
        if (!authorization.isAdmin() && !authorization.callerId().equals(existing.getExpertId())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the assigned expert or an administrator can delete this answer");
        }
        consultationAnswerRepository.deleteById(id);
    }
}
