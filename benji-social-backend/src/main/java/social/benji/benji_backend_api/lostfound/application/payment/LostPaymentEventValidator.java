package social.benji.benji_backend_api.lostfound.application.payment;

import org.springframework.stereotype.Component;
import social.benji.benji_backend_api.payment.application.event.PaymentSucceededEvent;
import social.benji.benji_backend_api.payment.application.PaymentValidationUseCase;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

import java.util.UUID;

@Component
public class LostPaymentEventValidator {
    private final PaymentValidationUseCase paymentValidationUseCase;

    public LostPaymentEventValidator(PaymentValidationUseCase paymentValidationUseCase) {
        this.paymentValidationUseCase = paymentValidationUseCase;
    }

    public PaymentValidationUseCase.ValidatedPaymentResult validate(
            PaymentSucceededEvent event,
            UUID expectedAdvertisementId,
            UUID expectedOwnerId) {
        if (event == null) {
            throw new IllegalArgumentException("Payment event is required");
        }
        if (event.transactionId() == null
                || event.userId() == null
                || event.serviceType() == null
                || event.serviceReferenceId() == null
                || event.amount() == null
                || event.currency() == null
                || event.gatewayReferenceId() == null
                || event.gatewayReferenceId().isBlank()) {
            throw new IllegalArgumentException("Malformed payment success event");
        }
        if (expectedAdvertisementId == null || expectedOwnerId == null) {
            throw new IllegalArgumentException("Payment validation expectations are incomplete");
        }
        if (event.serviceType() != PaidServiceType.LOST_PET_ADVERTISEMENT) {
            throw new IllegalArgumentException("Payment event is not for LOST_PET_ADVERTISEMENT");
        }
        return paymentValidationUseCase.validateSuccessfulPayment(
                event,
                PaidServiceType.LOST_PET_ADVERTISEMENT,
                expectedAdvertisementId,
                expectedOwnerId);
    }
}
