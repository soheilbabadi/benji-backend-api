package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import social.benji.benji_backend_api.consultation.domain.model.Consultation;
import social.benji.benji_backend_api.consultation.dto.ConsultationDto;

@Mapper(componentModel = "spring", uses = ConsultationAnswerMapper.class)
public interface ConsultationMapper {

    ConsultationDto toDto(Consultation consultation);

    Consultation toModel(ConsultationDto consultationDto);

    void updateModelFromDto(ConsultationDto dto, @MappingTarget Consultation consultation);
}
