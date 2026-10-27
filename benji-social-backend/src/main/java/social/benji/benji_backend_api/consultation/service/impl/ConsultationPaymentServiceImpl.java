package social.benji.benji_backend_api.consultation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.consultation.domain.valueobject.PaymentStatus;
import social.benji.benji_backend_api.consultation.dto.ConsultationPaymentDto;
import social.benji.benji_backend_api.consultation.mapper.ConsultationPaymentMapper;
import social.benji.benji_backend_api.consultation.repository.ConsultationPaymentRepository;
import social.benji.benji_backend_api.consultation.service.ConsultationPaymentService;
import social.benji.benji_backend_api.exception.BenjiCustomException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationPaymentServiceImpl implements ConsultationPaymentService {

    private final ConsultationPaymentRepository consultationPaymentRepository;
    private final ConsultationPaymentMapper consultationPaymentMapper;

    @Override
    public ConsultationPaymentDto create(ConsultationPaymentDto paymentDto) {
        return consultationPaymentMapper.toDto(consultationPaymentRepository.save(consultationPaymentMapper.toModel(paymentDto)));
    }

    @Override
    public ConsultationPaymentDto findById(String id) {
        return consultationPaymentRepository.findById(id)
                .map(consultationPaymentMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationPayment not found with id: " + id));
    }

    @Override
    public List<ConsultationPaymentDto> findAll() {
        return consultationPaymentRepository.findAll().stream().map(consultationPaymentMapper::toDto).toList();
    }

    @Override
    public ConsultationPaymentDto findByConsultationId(String consultationId) {
        return consultationPaymentRepository.findByConsultationId(consultationId)
                .map(consultationPaymentMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationPayment not found for consultationId: " + consultationId));
    }

    @Override
    public ConsultationPaymentDto findByProviderPaymentId(String providerPaymentId) {
        return consultationPaymentRepository.findByProviderPaymentId(providerPaymentId)
                .map(consultationPaymentMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationPayment not found for providerPaymentId: " + providerPaymentId));
    }

    @Override
    public ConsultationPaymentDto findByIdempotencyKey(String idempotencyKey) {
        return consultationPaymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(consultationPaymentMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("ConsultationPayment not found for idempotencyKey: " + idempotencyKey));
    }

    @Override
    public List<ConsultationPaymentDto> findByStatus(PaymentStatus status) {
        return consultationPaymentRepository.findByStatus(status).stream().map(consultationPaymentMapper::toDto).toList();
    }

    @Override
    public ConsultationPaymentDto update(ConsultationPaymentDto paymentDto) {
        return consultationPaymentMapper.toDto(consultationPaymentRepository.save(consultationPaymentMapper.toModel(paymentDto)));
    }

    @Override
    public void deleteById(String id) {
        if (!consultationPaymentRepository.existsById(id)) {
            throw new BenjiCustomException.ResourceNotFoundException("ConsultationPayment not found with id: " + id);
        }
        consultationPaymentRepository.deleteById(id);
    }
}
