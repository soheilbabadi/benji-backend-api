package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationAnswer;
import social.benji.benji_backend_api.consultation.dto.ConsultationAnswerDto;

@Mapper(componentModel = "spring")
public interface ConsultationAnswerMapper {

    ConsultationAnswerDto toDto(ConsultationAnswer answer);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "consultationId", source = "consultationId")
    @Mapping(target = "assessment", source = "assessment")
    @Mapping(target = "recommendedActions", source = "recommendedActions")
    @Mapping(target = "warningSigns", source = "warningSigns")
    @Mapping(target = "inPersonVisitRecommended", source = "inPersonVisitRecommended")
    @Mapping(target = "urgency", source = "urgency")
    ConsultationAnswer toModel(ConsultationAnswerDto answerDto);
}
