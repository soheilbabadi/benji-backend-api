package social.benji.benji_backend_api.consultation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import social.benji.benji_backend_api.consultation.domain.model.ConsultationPayment;
import social.benji.benji_backend_api.consultation.domain.valueobject.PaymentStatus;

import java.util.List;
import java.util.Optional;

public interface ConsultationPaymentRepository extends MongoRepository<ConsultationPayment, String> {

    Optional<ConsultationPayment> findByConsultationId(String consultationId);

    Optional<ConsultationPayment> findByProviderPaymentId(String providerPaymentId);

    Optional<ConsultationPayment> findByIdempotencyKey(String idempotencyKey);

    List<ConsultationPayment> findByStatus(PaymentStatus status);
}
