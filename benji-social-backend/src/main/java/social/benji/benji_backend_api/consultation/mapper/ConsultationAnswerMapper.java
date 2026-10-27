package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationAnswer;
import social.benji.benji_backend_api.consultation.dto.ConsultationAnswerDto;

@Mapper(componentModel = "spring")
public interface ConsultationAnswerMapper {

    ConsultationAnswerDto toDto(ConsultationAnswer answer);

    ConsultationAnswer toModel(ConsultationAnswerDto answerDto);

    void updateModelFromDto(ConsultationAnswerDto dto, @MappingTarget ConsultationAnswer answer);
}
