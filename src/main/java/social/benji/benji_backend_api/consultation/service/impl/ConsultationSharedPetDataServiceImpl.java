package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.domain.valueobject.PetDataType;
import social.benji.benji_backend_api.consultation.dto.ConsultationSharedPetDataDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationSharedPetDataMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationSharedPetDataRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationSharedPetDataService;
import social.benji.benji_backend_api.exception.BenjiCustomException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationSharedPetDataServiceImpl implements ConsultationSharedPetDataService {

    private final ConsultationSharedPetDataRepository consultationSharedPetDataRepository;
    private final ConsultationSharedPetDataMapper consultationSharedPetDataMapper;

    @Override
    public ConsultationSharedPetDataDto create(ConsultationSharedPetDataDto sharedPetDataDto) {
        return consultationSharedPetDataMapper.toDto(consultationSharedPetDataRepository.save(consultationSharedPetDataMapper.toModel(sharedPetDataDto)));
    }

    @Override
    public ConsultationSharedPetDataDto findById(String id) {
        return consultationSharedPetDataRepository.findById(id)
                .map(consultationSharedPetDataMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationSharedPetData not found with id: " + id));
    }

    @Override
    public List<ConsultationSharedPetDataDto> findAll() {
        return consultationSharedPetDataRepository.findAll().stream().map(consultationSharedPetDataMapper::toDto).toList();
    }

    @Override
    public List<ConsultationSharedPetDataDto> findByConsultationId(String consultationId) {
        return consultationSharedPetDataRepository.findByConsultationId(consultationId).stream().map(consultationSharedPetDataMapper::toDto).toList();
    }

    @Override
    public ConsultationSharedPetDataDto findByConsultationIdAndDataType(String consultationId, PetDataType dataType) {
        return consultationSharedPetDataRepository.findByConsultationIdAndDataType(consultationId, dataType)
                .map(consultationSharedPetDataMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationSharedPetData not found for consultationId: " + consultationId + " and dataType: " + dataType));
    }

    @Override
    public ConsultationSharedPetDataDto update(ConsultationSharedPetDataDto sharedPetDataDto) {
        return consultationSharedPetDataMapper.toDto(consultationSharedPetDataRepository.save(consultationSharedPetDataMapper.toModel(sharedPetDataDto)));
    }

    @Override
    public void deleteById(String id) {
        if (!consultationSharedPetDataRepository.existsById(id)) {
            throw new BenjiCustomException.ResourceNotFoundException("ConsultationSharedPetData not found with id: " + id);
        }
        consultationSharedPetDataRepository.deleteById(id);
    }
}
