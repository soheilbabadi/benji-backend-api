package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.dto.ConsultationMessageDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationMessageMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationMessageRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationMessageService;
import social.benji.benji_backend_api.exception.BenjiCustomException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationMessageServiceImpl implements ConsultationMessageService {

    private final ConsultationMessageRepository consultationMessageRepository;
    private final ConsultationMessageMapper consultationMessageMapper;

    @Override
    public ConsultationMessageDto create(ConsultationMessageDto messageDto) {
        return consultationMessageMapper.toDto(consultationMessageRepository.save(consultationMessageMapper.toModel(messageDto)));
    }

    @Override
    public ConsultationMessageDto findById(String id) {
        return consultationMessageRepository.findById(id)
                .map(consultationMessageMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationMessage not found with id: " + id));
    }

    @Override
    public List<ConsultationMessageDto> findAll() {
        return consultationMessageRepository.findAll().stream().map(consultationMessageMapper::toDto).toList();
    }

    @Override
    public List<ConsultationMessageDto> findByConsultationId(String consultationId) {
        return consultationMessageRepository.findByConsultationIdOrderByCreatedAtAsc(consultationId).stream().map(consultationMessageMapper::toDto).toList();
    }

    @Override
    public List<ConsultationMessageDto> findByConsultationIdAndSenderId(String consultationId, String senderId) {
        return consultationMessageRepository.findByConsultationIdAndSenderId(consultationId, senderId).stream().map(consultationMessageMapper::toDto).toList();
    }

    @Override
    public ConsultationMessageDto update(ConsultationMessageDto messageDto) {
        return consultationMessageMapper.toDto(consultationMessageRepository.save(consultationMessageMapper.toModel(messageDto)));
    }

    @Override
    public void deleteById(String id) {
        if (!consultationMessageRepository.existsById(id)) {
            throw new BenjiCustomException.ResourceNotFoundException("ConsultationMessage not found with id: " + id);
        }
        consultationMessageRepository.deleteById(id);
    }
}
