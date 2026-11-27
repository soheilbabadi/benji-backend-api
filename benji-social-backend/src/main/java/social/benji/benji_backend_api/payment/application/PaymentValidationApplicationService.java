package social.benji.benji_backend_api.payment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.payment.application.event.PaymentSucceededEvent;
import social.benji.benji_backend_api.payment.domain.model.PaymentTransaction;
import social.benji.benji_backend_api.payment.domain.repository.PaymentTransactionRepository;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;
import social.benji.benji_backend_api.payment.domain.valueobject.PaymentStatus;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentValidationApplicationService implements PaymentValidationUseCase {
    private final PaymentTransactionRepository transactionRepository;

    @Override
    public ValidatedPaymentResult validateSuccessfulPayment(
            PaymentSucceededEvent event,
            PaidServiceType expectedServiceType,
            UUID expectedServiceReferenceId,
            UUID expectedOwnerId) {
        if (event == null || event.transactionId() == null) {
            throw new IllegalArgumentException("Malformed payment success event");
        }
        if (event.serviceType() == null || event.serviceReferenceId() == null
                || event.userId() == null || event.gatewayReferenceId() == null
                || event.gatewayReferenceId().isBlank() || event.amount() == null
                || event.currency() == null) {
            throw new IllegalArgumentException("Malformed payment success event");
        }
        if (expectedServiceType == null || expectedServiceReferenceId == null
                || expectedOwnerId == null) {
            throw new IllegalArgumentException("Payment validation expectations are incomplete");
        }

        PaymentTransaction transaction = transactionRepository.findById(event.transactionId())
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction does not exist"));

        if (!Objects.equals(transaction.getId(), event.transactionId())) {
            throw new IllegalArgumentException("Payment transaction ID mismatch");
        }
        if (transaction.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Payment transaction is not successful");
        }
        if (transaction.getServiceType() != expectedServiceType
                || event.serviceType() != expectedServiceType) {
            throw new IllegalArgumentException("Payment service type mismatch");
        }
        if (!Objects.equals(transaction.getServiceReferenceId(), expectedServiceReferenceId)
                || !event.serviceReferenceId().equals(expectedServiceReferenceId)) {
            throw new IllegalArgumentException("Payment advertisement reference mismatch");
        }
        if (!Objects.equals(transaction.getUserId(), expectedOwnerId)
                || !event.userId().equals(expectedOwnerId)) {
            throw new IllegalArgumentException("Payment owner mismatch");
        }
        if (!"IRR".equals(transaction.getCurrency())
                || !"IRR".equals(event.currency())) {
            throw new IllegalArgumentException("Payment currency mismatch");
        }
        if (!Objects.equals(transaction.getAmount(), event.amount())
                || transaction.getAmount() == null || transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Payment amount mismatch");
        }
        if (!Objects.equals(transaction.getGatewayReferenceId(), event.gatewayReferenceId())) {
            throw new IllegalArgumentException("Gateway reference mismatch");
        }

        return new ValidatedPaymentResult(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getServiceType(),
                transaction.getServiceReferenceId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getGatewayReferenceId());
    }

    @Override
    public AuthoritativePaymentStatus lookupLatest(PaidServiceType serviceType,
                                                    UUID serviceReferenceId) {
        if (serviceType == null || serviceReferenceId == null) {
            throw new IllegalArgumentException("Payment lookup parameters are incomplete");
        }
        return transactionRepository.findTopByServiceTypeAndServiceReferenceIdOrderByCreatedAtDesc(
                        serviceType, serviceReferenceId)
                .map(transaction -> new AuthoritativePaymentStatus(
                        transaction.getId(),
                        transaction.getStatus(),
                        transaction.getUserId(),
                        transaction.getServiceType(),
                        transaction.getServiceReferenceId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getGatewayReferenceId()))
                .orElseGet(() -> new AuthoritativePaymentStatus(
                        null, PaymentStatus.UNKNOWN, null, serviceType,
                        serviceReferenceId, null, null, null));
    }
}
