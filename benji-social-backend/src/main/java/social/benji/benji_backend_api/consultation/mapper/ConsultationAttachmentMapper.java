package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationAttachment;
import social.benji.benji_backend_api.consultation.dto.ConsultationAttachmentDto;

@Mapper(componentModel = "spring")
public interface ConsultationAttachmentMapper {

    ConsultationAttachmentDto toDto(ConsultationAttachment attachment);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "consultationId", source = "consultationId")
    @Mapping(target = "originalFilename", source = "originalFilename")
    @Mapping(target = "contentType", source = "contentType")
    @Mapping(target = "fileSize", source = "fileSize")
    ConsultationAttachment toModel(ConsultationAttachmentDto attachmentDto);
}
