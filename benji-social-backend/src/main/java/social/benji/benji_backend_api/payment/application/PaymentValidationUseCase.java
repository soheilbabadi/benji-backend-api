package social.benji.benji_backend_api.payment.application;

import social.benji.benji_backend_api.payment.application.event.PaymentSucceededEvent;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;
import social.benji.benji_backend_api.payment.domain.valueobject.PaymentStatus;

import java.util.UUID;

public interface PaymentValidationUseCase {
    ValidatedPaymentResult validateSuccessfulPayment(
            PaymentSucceededEvent event,
            PaidServiceType expectedServiceType,
            UUID expectedServiceReferenceId,
            UUID expectedOwnerId);

    record ValidatedPaymentResult(
            UUID transactionId,
            UUID userId,
            PaidServiceType serviceType,
            UUID serviceReferenceId,
            Long amount,
            String currency,
            PaymentStatus status,
            String gatewayReferenceId
    ) {}

    record AuthoritativePaymentStatus(
            UUID transactionId,
            PaymentStatus status,
            UUID userId,
            PaidServiceType serviceType,
            UUID serviceReferenceId,
            Long amount,
            String currency,
            String gatewayReferenceId
    ) {}

    AuthoritativePaymentStatus lookupLatest(PaidServiceType serviceType, UUID serviceReferenceId);
}
