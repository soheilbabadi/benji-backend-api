package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.dto.ConsultationAnswerDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationAnswerMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationAnswerRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationAnswerService;
import social.benji.benji_backend_api.exception.BenjiCustomException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationAnswerServiceImpl implements ConsultationAnswerService {

    private final ConsultationAnswerRepository consultationAnswerRepository;
    private final ConsultationAnswerMapper consultationAnswerMapper;

    @Override
    public ConsultationAnswerDto create(ConsultationAnswerDto answerDto) {
        return consultationAnswerMapper.toDto(consultationAnswerRepository.save(consultationAnswerMapper.toModel(answerDto)));
    }

    @Override
    public ConsultationAnswerDto findById(String id) {
        return consultationAnswerRepository.findById(id)
                .map(consultationAnswerMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationAnswer not found with id: " + id));
    }

    @Override
    public List<ConsultationAnswerDto> findAll() {
        return consultationAnswerRepository.findAll().stream().map(consultationAnswerMapper::toDto).toList();
    }

    @Override
    public ConsultationAnswerDto findByExpertId(String expertId) {
        return consultationAnswerRepository.findByExpertId(expertId)
                .map(consultationAnswerMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationAnswer not found for expertId: " + expertId));
    }

    @Override
    public ConsultationAnswerDto update(ConsultationAnswerDto answerDto) {
        return consultationAnswerMapper.toDto(consultationAnswerRepository.save(consultationAnswerMapper.toModel(answerDto)));
    }

    @Override
    public void deleteById(String id) {
        if (!consultationAnswerRepository.existsById(id)) {
            throw new BenjiCustomException.ResourceNotFoundException("ConsultationAnswer not found with id: " + id);
        }
        consultationAnswerRepository.deleteById(id);
    }
}
