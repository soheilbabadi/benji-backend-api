package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.domain.valueobject.ExpertVerificationStatus;
import social.benji.benji_backend_api.consultation.dto.ExpertProfileDto;
import social.benji.benji_backend_api.consultation.mapper.ExpertProfileMapper;
import social.benji.benji_backend_api.consultation.repository.ExpertProfileRepository;
import social.benji.benji_backend_api.consultation.service.ExpertProfileService;
import social.benji.benji_backend_api.exception.BenjiCustomException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpertProfileServiceImpl implements ExpertProfileService {

    private final ExpertProfileRepository expertProfileRepository;
    private final ExpertProfileMapper expertProfileMapper;

    @Override
    public ExpertProfileDto create(ExpertProfileDto expertProfileDto) {
        return expertProfileMapper.toDto(expertProfileRepository.save(expertProfileMapper.toModel(expertProfileDto)));
    }

    @Override
    public ExpertProfileDto findById(String id) {
        return expertProfileRepository.findById(id)
                .map(expertProfileMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ExpertProfile not found with id: " + id));
    }

    @Override
    public List<ExpertProfileDto> findAll() {
        return expertProfileRepository.findAll().stream().map(expertProfileMapper::toDto).toList();
    }

    @Override
    public ExpertProfileDto findByUserId(String userId) {
        return expertProfileRepository.findByUserId(userId)
                .map(expertProfileMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ExpertProfile not found for userId: " + userId));
    }

    @Override
    public List<ExpertProfileDto> findByVerificationStatus(ExpertVerificationStatus verificationStatus) {
        return expertProfileRepository.findByVerificationStatus(verificationStatus).stream().map(expertProfileMapper::toDto).toList();
    }

    @Override
    public List<ExpertProfileDto> findAllActive() {
        return expertProfileRepository.findByIsActiveTrue().stream().map(expertProfileMapper::toDto).toList();
    }

    @Override
    public List<ExpertProfileDto> findBySpecialty(String specialty) {
        return expertProfileRepository.findBySpecialtiesContaining(specialty).stream().map(expertProfileMapper::toDto).toList();
    }

    @Override
    public ExpertProfileDto update(ExpertProfileDto expertProfileDto) {
        return expertProfileMapper.toDto(expertProfileRepository.save(expertProfileMapper.toModel(expertProfileDto)));
    }

    @Override
    public void deleteById(String id) {
        if (!expertProfileRepository.existsById(id)) {
            throw new BenjiCustomException.ResourceNotFoundException("ExpertProfile not found with id: " + id);
        }
        expertProfileRepository.deleteById(id);
    }
}
