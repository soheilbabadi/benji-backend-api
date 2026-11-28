package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import social.benji.benji_backend_api.consultation.domain.model.Consultation;
import social.benji.benji_backend_api.consultation.dto.ConsultationDto;

@Mapper(componentModel = "spring", uses = ConsultationAnswerMapper.class)
public interface ConsultationMapper {

    ConsultationDto toDto(Consultation consultation);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "petId", source = "petId")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "question", source = "question")
    @Mapping(target = "emergencyDisclaimerAccepted", source = "emergencyDisclaimerAccepted")
    Consultation toModel(ConsultationDto consultationDto);
}
