package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationSharedPetData;
import social.benji.benji_backend_api.consultation.dto.ConsultationSharedPetDataDto;

@Mapper(componentModel = "spring")
public interface ConsultationSharedPetDataMapper {

    ConsultationSharedPetDataDto toDto(ConsultationSharedPetData sharedPetData);

    ConsultationSharedPetData toModel(ConsultationSharedPetDataDto sharedPetDataDto);

    void updateModelFromDto(ConsultationSharedPetDataDto dto, @MappingTarget ConsultationSharedPetData sharedPetData);
}
