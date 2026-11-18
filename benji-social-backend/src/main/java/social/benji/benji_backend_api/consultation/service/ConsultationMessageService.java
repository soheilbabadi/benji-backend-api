package social.benji.benji_backend_api.consultation.service;

import social.benji.benji_backend_api.consultation.dto.ConsultationMessageDto;

import java.util.List;

public interface ConsultationMessageService {

    ConsultationMessageDto create(ConsultationMessageDto messageDto);

    ConsultationMessageDto findById(String id);

    List<ConsultationMessageDto> findAll();

    List<ConsultationMessageDto> findByConsultationId(String consultationId);

    List<ConsultationMessageDto> findByConsultationIdAndSenderId(String consultationId, String senderId);

    ConsultationMessageDto update(ConsultationMessageDto messageDto);

    void deleteById(String id);
}
