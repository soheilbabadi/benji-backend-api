package social.benji.benji_backend_api.lostfound.application.payment;

import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;
import social.benji.benji_backend_api.payment.infrastructure.event.PaymentSucceededEvent;

import java.util.UUID;

public final class LostPaymentEventValidator {
    private LostPaymentEventValidator() {}

    public static void validate(PaymentSucceededEvent event, UUID expectedAdvertisementId) {
        if (event == null) {
            throw new IllegalArgumentException("Payment event is required");
        }
        if (event.serviceType() != PaidServiceType.LOST_PET_ADVERTISEMENT) {
            throw new IllegalArgumentException("Payment event is not for LOST_PET_ADVERTISEMENT");
        }
        if (event.serviceReferenceId() == null
                || !event.serviceReferenceId().equals(expectedAdvertisementId)) {
            throw new IllegalArgumentException("Payment event references a different advertisement");
        }
        if (event.transactionId() == null || event.gatewayReferenceId() == null
                || event.gatewayReferenceId().isBlank()) {
            throw new IllegalArgumentException("Payment event has no verified transaction identity");
        }
    }
}
