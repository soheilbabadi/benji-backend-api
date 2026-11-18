package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.dto.ConsultationAttachmentDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationAttachmentMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationAttachmentRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationAttachmentService;
import social.benji.benji_backend_api.exception.BenjiCustomException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationAttachmentServiceImpl implements ConsultationAttachmentService {

    private final ConsultationAttachmentRepository consultationAttachmentRepository;
    private final ConsultationAttachmentMapper consultationAttachmentMapper;

    @Override
    public ConsultationAttachmentDto create(ConsultationAttachmentDto attachmentDto) {
        return consultationAttachmentMapper.toDto(consultationAttachmentRepository.save(consultationAttachmentMapper.toModel(attachmentDto)));
    }

    @Override
    public ConsultationAttachmentDto findById(String id) {
        return consultationAttachmentRepository.findById(id)
                .map(consultationAttachmentMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationAttachment not found with id: " + id));
    }

    @Override
    public List<ConsultationAttachmentDto> findAll() {
        return consultationAttachmentRepository.findAll().stream().map(consultationAttachmentMapper::toDto).toList();
    }

    @Override
    public List<ConsultationAttachmentDto> findByConsultationId(String consultationId) {
        return consultationAttachmentRepository.findByConsultationId(consultationId).stream().map(consultationAttachmentMapper::toDto).toList();
    }

    @Override
    public List<ConsultationAttachmentDto> findByUploadedBy(String uploadedBy) {
        return consultationAttachmentRepository.findByUploadedBy(uploadedBy).stream().map(consultationAttachmentMapper::toDto).toList();
    }

    @Override
    public ConsultationAttachmentDto update(ConsultationAttachmentDto attachmentDto) {
        return consultationAttachmentMapper.toDto(consultationAttachmentRepository.save(consultationAttachmentMapper.toModel(attachmentDto)));
    }

    @Override
    public void deleteById(String id) {
        if (!consultationAttachmentRepository.existsById(id)) {
            throw new BenjiCustomException.ResourceNotFoundException("ConsultationAttachment not found with id: " + id);
        }
        consultationAttachmentRepository.deleteById(id);
    }
}
