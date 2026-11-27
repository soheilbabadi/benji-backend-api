package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.dto.ConsultationMessageDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationMessageMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationMessageRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationMessageService;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.consultation.security.ConsultationAuthorizationService;
import social.benji.benji_backend_api.consultation.repository.ConsultationRepository;
import social.benji.benji_backend_api.consultation.domain.model.Consultation;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationMessage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationMessageServiceImpl implements ConsultationMessageService {

    private final ConsultationMessageRepository consultationMessageRepository;
    private final ConsultationMessageMapper consultationMessageMapper;
    private final ConsultationAuthorizationService authorization;
    private final ConsultationRepository consultationRepository;

    @Override
    public ConsultationMessageDto create(ConsultationMessageDto messageDto) {
        if (messageDto == null || messageDto.getConsultationId() == null
                || messageDto.getConsultationId().isBlank()) {
            throw new BenjiCustomException.ValidationException("Consultation ID is required");
        }
        Consultation consultation = authorization.requireParticipant(messageDto.getConsultationId());
        String callerId = authorization.callerId();
        ConsultationMessage message = consultationMessageMapper.toModel(messageDto);
        message.setSenderId(callerId);
        if (callerId.equals(consultation.getOwnerId())) {
            message.setSenderRole("OWNER");
        } else if (callerId.equals(consultation.getAssignedExpertId())) {
            message.setSenderRole("EXPERT");
        } else {
            message.setSenderRole("SYSTEM");
        }
        message.setSystemMessage(false);
        message.setCreatedAt(java.time.Instant.now());
        return consultationMessageMapper.toDto(consultationMessageRepository.save(message));
    }

    @Override
    public ConsultationMessageDto findById(String id) {
        ConsultationMessage message = consultationMessageRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationMessage not found"));
        authorization.requireParticipant(message.getConsultationId());
        return consultationMessageMapper.toDto(message);
    }

    @Override
    public List<ConsultationMessageDto> findAll() {
        if (authorization.isAdmin()) {
            return consultationMessageRepository.findAll().stream().map(consultationMessageMapper::toDto).toList();
        }
        String callerId = authorization.callerId();
        return consultationRepository.findAll().stream()
                .filter(cons -> callerId.equals(cons.getOwnerId())
                        || callerId.equals(cons.getAssignedExpertId()))
                .flatMap(cons -> consultationMessageRepository
                        .findByConsultationIdOrderByCreatedAtAsc(cons.getId()).stream())
                .map(consultationMessageMapper::toDto)
                .toList();
    }

    @Override
    public List<ConsultationMessageDto> findByConsultationId(String consultationId) {
        authorization.requireParticipant(consultationId);
        return consultationMessageRepository.findByConsultationIdOrderByCreatedAtAsc(consultationId).stream().map(consultationMessageMapper::toDto).toList();
    }

    @Override
    public List<ConsultationMessageDto> findByConsultationIdAndSenderId(String consultationId, String senderId) {
        authorization.requireParticipant(consultationId);
        if (!authorization.isAdmin() && !authorization.callerId().equals(senderId)) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "A participant can only query their own messages");
        }
        return consultationMessageRepository.findByConsultationIdAndSenderId(consultationId, senderId).stream().map(consultationMessageMapper::toDto).toList();
    }

    @Override
    public ConsultationMessageDto update(ConsultationMessageDto messageDto) {
        if (messageDto == null || messageDto.getId() == null) {
            throw new BenjiCustomException.ValidationException("Message ID is required");
        }
        ConsultationMessage existing = consultationMessageRepository.findById(messageDto.getId())
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationMessage not found"));
        authorization.requireParticipant(existing.getConsultationId());
        if (!authorization.isAdmin() && !authorization.callerId().equals(existing.getSenderId())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the sender or an administrator can update this message");
        }
        existing.setContent(messageDto.getContent());
        return consultationMessageMapper.toDto(consultationMessageRepository.save(existing));
    }

    @Override
    public void deleteById(String id) {
        ConsultationMessage existing = consultationMessageRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationMessage not found"));
        authorization.requireParticipant(existing.getConsultationId());
        if (!authorization.isAdmin() && !authorization.callerId().equals(existing.getSenderId())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the sender or an administrator can delete this message");
        }
        consultationMessageRepository.deleteById(id);
    }
}
