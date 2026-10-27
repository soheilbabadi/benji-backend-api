package social.benji.benji_backend_api.consultation.service;

import social.benji.benji_backend_api.consultation.domain.valueobject.ExpertVerificationStatus;
import social.benji.benji_backend_api.consultation.dto.ExpertProfileDto;

import java.util.List;

public interface ExpertProfileService {

    ExpertProfileDto create(ExpertProfileDto expertProfileDto);

    ExpertProfileDto findById(String id);

    List<ExpertProfileDto> findAll();

    ExpertProfileDto findByUserId(String userId);

    List<ExpertProfileDto> findByVerificationStatus(ExpertVerificationStatus verificationStatus);

    List<ExpertProfileDto> findAllActive();

    List<ExpertProfileDto> findBySpecialty(String specialty);

    ExpertProfileDto update(ExpertProfileDto expertProfileDto);

    void deleteById(String id);
}
