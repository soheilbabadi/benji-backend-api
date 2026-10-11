package social.benji.benji_backend_api.consultation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationPayment;
import social.benji.benji_backend_api.consultation.dto.ConsultationPaymentDto;

@Mapper(componentModel = "spring")
public interface ConsultationPaymentMapper {

    ConsultationPaymentDto toDto(ConsultationPayment payment);

    ConsultationPayment toModel(ConsultationPaymentDto paymentDto);

    void updateModelFromDto(ConsultationPaymentDto dto, @MappingTarget ConsultationPayment payment);
}
