package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationPayment;
import social.benji.benji_backend_api.consultation.dto.ConsultationPaymentDto;

@Mapper(componentModel = "spring")
public interface ConsultationPaymentMapper {

    ConsultationPaymentDto toDto(ConsultationPayment payment);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "consultationId", source = "consultationId")
    ConsultationPayment toModel(ConsultationPaymentDto paymentDto);
}
