package social.benji.benji_backend_api.lostfound.application.payment;

import social.benji.benji_backend_api.payment.application.event.PaymentSucceededEvent;
import social.benji.benji_backend_api.payment.application.PaymentValidationUseCase;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

import java.util.UUID;

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
