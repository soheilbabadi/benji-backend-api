package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import social.benji.benji_backend_api.consultation.domain.model.ExpertProfile;
import social.benji.benji_backend_api.consultation.dto.ExpertProfileDto;

@Mapper(componentModel = "spring")
public interface ExpertProfileMapper {

    ExpertProfileDto toDto(ExpertProfile expertProfile);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "bio", source = "bio")
    @Mapping(target = "specialties", source = "specialties")
    ExpertProfile toModel(ExpertProfileDto expertProfileDto);
}
