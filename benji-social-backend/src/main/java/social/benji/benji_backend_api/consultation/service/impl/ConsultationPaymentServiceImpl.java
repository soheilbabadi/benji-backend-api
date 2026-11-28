package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.domain.valueobject.PaymentStatus;
import social.benji.benji_backend_api.consultation.dto.ConsultationPaymentDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationPaymentMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationPaymentRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationPaymentService;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.consultation.security.ConsultationAuthorizationService;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationPayment;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationPaymentServiceImpl implements ConsultationPaymentService {

    private final ConsultationPaymentRepository consultationPaymentRepository;
    private final ConsultationPaymentMapper consultationPaymentMapper;
    private final ConsultationAuthorizationService authorization;

    @Override
    public ConsultationPaymentDto create(ConsultationPaymentDto paymentDto) {
        if (paymentDto == null || paymentDto.getConsultationId() == null
                || paymentDto.getConsultationId().isBlank()) {
            throw new BenjiCustomException.ValidationException("Consultation ID is required");
        }
        social.benji.benji_backend_api.consultation.domain.model.Consultation consultation =
                authorization.requireOwner(paymentDto.getConsultationId());
        if (consultation.getPrice() == null) {
            throw new BenjiCustomException.ValidationException(
                    "Consultation price is required before payment creation");
        }
        ConsultationPayment payment = consultationPaymentMapper.toModel(paymentDto);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(consultation.getPrice());
        payment.setProviderPaymentId(null);
        payment.setIdempotencyKey(null);
        java.time.Instant now = java.time.Instant.now();
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        payment.setPaidAt(null);
        return consultationPaymentMapper.toDto(consultationPaymentRepository.save(payment));
    }

    @Override
    public ConsultationPaymentDto findById(String id) {
        ConsultationPayment payment = consultationPaymentRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationPayment not found"));
        authorization.requireOwner(payment.getConsultationId());
        return consultationPaymentMapper.toDto(payment);
    }

    @Override
    public List<ConsultationPaymentDto> findAll() {
        authorization.requireAdmin();
        return consultationPaymentRepository.findAll().stream().map(consultationPaymentMapper::toDto).toList();
    }

    @Override
    public ConsultationPaymentDto findByConsultationId(String consultationId) {
        authorization.requireOwner(consultationId);
        return consultationPaymentRepository.findByConsultationId(consultationId)
                .map(consultationPaymentMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationPayment not found for consultationId: " + consultationId));
    }

    @Override
    public ConsultationPaymentDto findByProviderPaymentId(String providerPaymentId) {
        ConsultationPayment payment = consultationPaymentRepository.findByProviderPaymentId(providerPaymentId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationPayment not found for providerPaymentId: " + providerPaymentId));
        authorization.requireOwner(payment.getConsultationId());
        return consultationPaymentMapper.toDto(payment);
    }

    @Override
    public ConsultationPaymentDto findByIdempotencyKey(String idempotencyKey) {
        ConsultationPayment payment = consultationPaymentRepository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationPayment not found for idempotencyKey: " + idempotencyKey));
        authorization.requireOwner(payment.getConsultationId());
        return consultationPaymentMapper.toDto(payment);
    }

    @Override
    public List<ConsultationPaymentDto> findByStatus(PaymentStatus status) {
        authorization.requireAdmin();
        return consultationPaymentRepository.findByStatus(status).stream().map(consultationPaymentMapper::toDto).toList();
    }

    @Override
    public ConsultationPaymentDto update(ConsultationPaymentDto paymentDto) {
        authorization.requireAdmin();
        if (paymentDto == null || paymentDto.getId() == null) {
            throw new BenjiCustomException.ValidationException("Payment ID is required");
        }
        ConsultationPayment existing = consultationPaymentRepository.findById(paymentDto.getId())
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationPayment not found"));
        existing.setUpdatedAt(java.time.Instant.now());
        return consultationPaymentMapper.toDto(consultationPaymentRepository.save(existing));
    }

    @Override
    public void deleteById(String id) {
        authorization.requireAdmin();
        if (!consultationPaymentRepository.existsById(id)) {
            throw new BenjiCustomException.ResourceNotFoundException("ConsultationPayment not found with id: " + id);
        }
        consultationPaymentRepository.deleteById(id);
    }
}
