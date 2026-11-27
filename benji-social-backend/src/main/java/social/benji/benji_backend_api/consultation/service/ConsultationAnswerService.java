package social.benji.benji_backend_api.consultation.service;

import social.benji.benji_backend_api.consultation.dto.ConsultationAnswerDto;

import java.util.List;

public interface ConsultationAnswerService {

    ConsultationAnswerDto create(ConsultationAnswerDto answerDto);

    ConsultationAnswerDto findById(String id);

    List<ConsultationAnswerDto> findAll();

    ConsultationAnswerDto findByExpertId(String expertId);

    ConsultationAnswerDto update(ConsultationAnswerDto answerDto);

    void deleteById(String id);
}
