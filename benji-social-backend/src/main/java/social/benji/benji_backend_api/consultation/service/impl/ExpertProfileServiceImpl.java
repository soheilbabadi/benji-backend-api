package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.domain.valueobject.ExpertVerificationStatus;
import social.benji.benji_backend_api.consultation.dto.ExpertProfileDto;
import social.benji.benji_backend_api.consultation.mapper.ExpertProfileMapper;
import social.benji.benji_backend_api.consultation.repository.ExpertProfileRepository;
import social.benji.benji_backend_api.consultation.service.ExpertProfileService;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.consultation.security.ConsultationAuthorizationService;
import social.benji.benji_backend_api.consultation.domain.model.ExpertProfile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpertProfileServiceImpl implements ExpertProfileService {

    private final ExpertProfileRepository expertProfileRepository;
    private final ExpertProfileMapper expertProfileMapper;
    private final ConsultationAuthorizationService authorization;

    @Override
    public ExpertProfileDto create(ExpertProfileDto expertProfileDto) {
        String callerId = authorization.callerId();
        if (expertProfileRepository.findByUserId(callerId).isPresent()) {
            throw new BenjiCustomException.DuplicateEntityException("Expert profile already exists");
        }
        ExpertProfile profile = expertProfileMapper.toModel(expertProfileDto);
        profile.setUserId(callerId);
        profile.setVerificationStatus(ExpertVerificationStatus.PENDING);
        profile.setActive(false);
        java.time.Instant now = java.time.Instant.now();
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        return expertProfileMapper.toDto(expertProfileRepository.save(profile));
    }

    @Override
    public ExpertProfileDto findById(String id) {
        return expertProfileRepository.findById(id)
                .map(this::toPublicDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ExpertProfile not found with id: " + id));
    }

    @Override
    public List<ExpertProfileDto> findAll() {
        return expertProfileRepository.findAll().stream().map(this::toPublicDto).toList();
    }

    @Override
    public ExpertProfileDto findByUserId(String userId) {
        authorization.requireOwnerOrAdminForUserId(userId);
        return expertProfileRepository.findByUserId(userId)
                .map(expertProfileMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ExpertProfile not found for userId: " + userId));
    }

    @Override
    public List<ExpertProfileDto> findByVerificationStatus(ExpertVerificationStatus verificationStatus) {
        return expertProfileRepository.findByVerificationStatus(verificationStatus).stream()
                .map(this::toPublicDto)
                .toList();
    }

    @Override
    public List<ExpertProfileDto> findAllActive() {
        return expertProfileRepository.findByIsActiveTrue().stream().map(this::toPublicDto).toList();
    }

    @Override
    public List<ExpertProfileDto> findBySpecialty(String specialty) {
        return expertProfileRepository.findBySpecialtiesContaining(specialty).stream()
                .map(this::toPublicDto)
                .toList();
    }

    @Override
    public ExpertProfileDto update(ExpertProfileDto expertProfileDto) {
        if (expertProfileDto == null || expertProfileDto.getId() == null) {
            throw new BenjiCustomException.ValidationException("Expert profile ID is required");
        }
        ExpertProfile existing = expertProfileRepository.findById(expertProfileDto.getId())
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ExpertProfile not found"));
        if (!authorization.isAdmin() && !authorization.callerId().equals(existing.getUserId())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the profile owner or an administrator can update this expert profile");
        }
        if (expertProfileDto.getBio() != null) existing.setBio(expertProfileDto.getBio());
        if (expertProfileDto.getSpecialties() != null) existing.setSpecialties(expertProfileDto.getSpecialties());
        if (authorization.isAdmin()) {
            if (expertProfileDto.getVerificationStatus() != null) {
                existing.setVerificationStatus(expertProfileDto.getVerificationStatus());
            }
        }
        existing.setUpdatedAt(java.time.Instant.now());
        ExpertProfileDto dto = expertProfileMapper.toDto(expertProfileRepository.save(existing));
        dto.setUserId(null);
        return dto;
    }

    @Override
    public void deleteById(String id) {
        ExpertProfile existing = expertProfileRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ExpertProfile not found"));
        if (!authorization.isAdmin() && !authorization.callerId().equals(existing.getUserId())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the profile owner or an administrator can delete this expert profile");
        }
        expertProfileRepository.deleteById(id);
    }

    private ExpertProfileDto toPublicDto(ExpertProfile profile) {
        ExpertProfileDto dto = expertProfileMapper.toDto(profile);
        dto.setUserId(null);
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);
        return dto;
    }
}
