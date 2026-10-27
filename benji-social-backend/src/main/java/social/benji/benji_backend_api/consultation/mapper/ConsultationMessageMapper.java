package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationMessage;
import social.benji.benji_backend_api.consultation.dto.ConsultationMessageDto;

@Mapper(componentModel = "spring")
public interface ConsultationMessageMapper {

    @Mapping(source = "systemMessage", target = "systemMessage")
    ConsultationMessageDto toDto(ConsultationMessage message);

    ConsultationMessage toModel(ConsultationMessageDto messageDto);

    void updateModelFromDto(ConsultationMessageDto dto, @MappingTarget ConsultationMessage message);
}
