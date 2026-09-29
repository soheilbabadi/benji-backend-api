package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationAttachment;
import social.benji.benji_backend_api.consultation.dto.ConsultationAttachmentDto;

@Mapper(componentModel = "spring")
public interface ConsultationAttachmentMapper {

    ConsultationAttachmentDto toDto(ConsultationAttachment attachment);

    ConsultationAttachment toModel(ConsultationAttachmentDto attachmentDto);

    void updateModelFromDto(ConsultationAttachmentDto dto, @MappingTarget ConsultationAttachment attachment);
}
