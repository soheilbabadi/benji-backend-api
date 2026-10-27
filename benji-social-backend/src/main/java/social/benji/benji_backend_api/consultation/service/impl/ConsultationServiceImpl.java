package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.domain.valueobject.ConsultationStatus;
import social.benji.benji_backend_api.consultation.dto.ConsultationDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationService;
import social.benji.benji_backend_api.exception.BenjiCustomException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final ConsultationMapper consultationMapper;

    @Override
    public ConsultationDto create(ConsultationDto consultationDto) {
        return consultationMapper.toDto(consultationRepository.save(consultationMapper.toModel(consultationDto)));
    }

    @Override
    public ConsultationDto findById(String id) {
        return consultationRepository.findById(id)
                .map(consultationMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Consultation not found with id: " + id));
    }

    @Override
    public List<ConsultationDto> findAll() {
        return consultationRepository.findAll().stream().map(consultationMapper::toDto).toList();
    }

    @Override
    public List<ConsultationDto> findByOwnerId(String ownerId) {
        return consultationRepository.findByOwnerId(ownerId).stream().map(consultationMapper::toDto).toList();
    }

    @Override
    public List<ConsultationDto> findByAssignedExpertId(String expertId) {
        return consultationRepository.findByAssignedExpertId(expertId).stream().map(consultationMapper::toDto).toList();
    }

    @Override
    public List<ConsultationDto> findByStatus(ConsultationStatus status) {
        return consultationRepository.findByStatus(status).stream().map(consultationMapper::toDto).toList();
    }

    @Override
    public List<ConsultationDto> findByOwnerIdAndStatus(String ownerId, ConsultationStatus status) {
        return consultationRepository.findByOwnerIdAndStatus(ownerId, status).stream().map(consultationMapper::toDto).toList();
    }

    @Override
    public ConsultationDto update(ConsultationDto consultationDto) {
        return consultationMapper.toDto(consultationRepository.save(consultationMapper.toModel(consultationDto)));
    }

    @Override
    public void deleteById(String id) {
        if (!consultationRepository.existsById(id)) {
            throw new BenjiCustomException.ResourceNotFoundException("Consultation not found with id: " + id);
        }
        consultationRepository.deleteById(id);
    }
}
