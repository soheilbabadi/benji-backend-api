package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationSharedPetData;
import social.benji.benji_backend_api.consultation.dto.ConsultationSharedPetDataDto;

@Mapper(componentModel = "spring")
public interface ConsultationSharedPetDataMapper {

    ConsultationSharedPetDataDto toDto(ConsultationSharedPetData sharedPetData);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "consultationId", source = "consultationId")
    @Mapping(target = "dataType", source = "dataType")
    @Mapping(target = "contentSnapshot", source = "contentSnapshot")
    ConsultationSharedPetData toModel(ConsultationSharedPetDataDto sharedPetDataDto);
}
