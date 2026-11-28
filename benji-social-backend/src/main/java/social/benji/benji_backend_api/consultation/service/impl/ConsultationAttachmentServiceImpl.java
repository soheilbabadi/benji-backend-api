package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.dto.ConsultationAttachmentDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationAttachmentMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationAttachmentRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationAttachmentService;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.consultation.security.ConsultationAuthorizationService;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationAttachment;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationAttachmentServiceImpl implements ConsultationAttachmentService {

    private final ConsultationAttachmentRepository consultationAttachmentRepository;
    private final ConsultationAttachmentMapper consultationAttachmentMapper;
    private final ConsultationAuthorizationService authorization;

    @Override
    public ConsultationAttachmentDto create(ConsultationAttachmentDto attachmentDto) {
        if (attachmentDto == null || attachmentDto.getConsultationId() == null
                || attachmentDto.getConsultationId().isBlank()) {
            throw new BenjiCustomException.ValidationException("Consultation ID is required");
        }
        authorization.requireParticipant(attachmentDto.getConsultationId());
        ConsultationAttachment attachment = consultationAttachmentMapper.toModel(attachmentDto);
        attachment.setUploadedBy(authorization.callerId());
        attachment.setStorageKey(null);
        attachment.setCreatedAt(java.time.Instant.now());
        return consultationAttachmentMapper.toDto(consultationAttachmentRepository.save(attachment));
    }

    @Override
    public ConsultationAttachmentDto findById(String id) {
        ConsultationAttachment attachment = consultationAttachmentRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationAttachment not found"));
        authorization.requireParticipant(attachment.getConsultationId());
        return consultationAttachmentMapper.toDto(attachment);
    }

    @Override
    public List<ConsultationAttachmentDto> findAll() {
        authorization.requireAdmin();
        return consultationAttachmentRepository.findAll().stream()
                .map(consultationAttachmentMapper::toDto)
                .toList();
    }

    @Override
    public List<ConsultationAttachmentDto> findByConsultationId(String consultationId) {
        authorization.requireParticipant(consultationId);
        return consultationAttachmentRepository.findByConsultationId(consultationId).stream().map(consultationAttachmentMapper::toDto).toList();
    }

    @Override
    public List<ConsultationAttachmentDto> findByUploadedBy(String uploadedBy) {
        authorization.requireOwnerOrAdminForUserId(uploadedBy);
        java.util.Set<String> consultationIds = authorization.isAdmin()
                ? null
                : authorization.accessibleConsultations().stream()
                        .map(consultation -> consultation.getId())
                        .collect(java.util.stream.Collectors.toSet());
        return consultationAttachmentRepository.findByUploadedBy(uploadedBy).stream()
                .filter(attachment -> consultationIds == null
                        || consultationIds.contains(attachment.getConsultationId()))
                .map(consultationAttachmentMapper::toDto)
                .toList();
    }

    @Override
    public ConsultationAttachmentDto update(ConsultationAttachmentDto attachmentDto) {
        if (attachmentDto == null || attachmentDto.getId() == null) {
            throw new BenjiCustomException.ValidationException("Attachment ID is required");
        }
        ConsultationAttachment existing = consultationAttachmentRepository.findById(attachmentDto.getId())
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationAttachment not found"));
        authorization.requireParticipant(existing.getConsultationId());
        if (!authorization.isAdmin() && !authorization.callerId().equals(existing.getUploadedBy())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the uploader or an administrator can update this attachment");
        }
        if (attachmentDto.getOriginalFilename() != null) existing.setOriginalFilename(attachmentDto.getOriginalFilename());
        if (attachmentDto.getContentType() != null) existing.setContentType(attachmentDto.getContentType());
        existing.setFileSize(attachmentDto.getFileSize());
        return consultationAttachmentMapper.toDto(consultationAttachmentRepository.save(existing));
    }

    @Override
    public void deleteById(String id) {
        ConsultationAttachment existing = consultationAttachmentRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationAttachment not found"));
        authorization.requireParticipant(existing.getConsultationId());
        if (!authorization.isAdmin() && !authorization.callerId().equals(existing.getUploadedBy())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the uploader or an administrator can delete this attachment");
        }
        consultationAttachmentRepository.deleteById(id);
    }
}
