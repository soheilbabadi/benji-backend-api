package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationMessage;
import social.benji.benji_backend_api.consultation.dto.ConsultationMessageDto;

@Mapper(componentModel = "spring")
public interface ConsultationMessageMapper {

    @Mapping(source = "systemMessage", target = "systemMessage")
    ConsultationMessageDto toDto(ConsultationMessage message);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "consultationId", source = "consultationId")
    @Mapping(target = "content", source = "content")
    ConsultationMessage toModel(ConsultationMessageDto messageDto);
}
