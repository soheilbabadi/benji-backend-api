package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import social.benji.benji_backend_api.consultation.domain.model.ExpertProfile;
import social.benji.benji_backend_api.consultation.dto.ExpertProfileDto;

@Mapper(componentModel = "spring")
public interface ExpertProfileMapper {

    ExpertProfileDto toDto(ExpertProfile expertProfile);

    ExpertProfile toModel(ExpertProfileDto expertProfileDto);

    void updateModelFromDto(ExpertProfileDto dto, @MappingTarget ExpertProfile expertProfile);
}
