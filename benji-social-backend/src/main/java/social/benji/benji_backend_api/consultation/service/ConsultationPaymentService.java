package social.benji.benji_backend_api.consultation.service;

import social.benji.benji_backend_api.consultation.domain.valueobject.PaymentStatus;
import social.benji.benji_backend_api.consultation.dto.ConsultationPaymentDto;

import java.util.List;

public interface ConsultationPaymentService {

    ConsultationPaymentDto create(ConsultationPaymentDto paymentDto);

    ConsultationPaymentDto findById(String id);

    List<ConsultationPaymentDto> findAll();

    ConsultationPaymentDto findByConsultationId(String consultationId);

    ConsultationPaymentDto findByProviderPaymentId(String providerPaymentId);

    ConsultationPaymentDto findByIdempotencyKey(String idempotencyKey);

    List<ConsultationPaymentDto> findByStatus(PaymentStatus status);

    ConsultationPaymentDto update(ConsultationPaymentDto paymentDto);

    void deleteById(String id);
}
