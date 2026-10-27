package social.benji.benji_backend_api.consultation.service;

import social.benji.benji_backend_api.consultation.dto.ConsultationAttachmentDto;

import java.util.List;

public interface ConsultationAttachmentService {

    ConsultationAttachmentDto create(ConsultationAttachmentDto attachmentDto);

    ConsultationAttachmentDto findById(String id);

    List<ConsultationAttachmentDto> findAll();

    List<ConsultationAttachmentDto> findByConsultationId(String consultationId);

    List<ConsultationAttachmentDto> findByUploadedBy(String uploadedBy);

    ConsultationAttachmentDto update(ConsultationAttachmentDto attachmentDto);

    void deleteById(String id);
}
