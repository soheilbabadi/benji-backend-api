package social.benji.benji_backend_api.lostfound.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import social.benji.benji_backend_api.lostfound.event.PaymentSucceededContext;

/**
 * Receives the payment module's published success notification (relayed from payment_event_outbox).
 * Kept separate from the activation service so the trust check stays in one visible place.
 */
@Component
class PaymentSuccessEventListener {

    private final LostFoundActivationService activationService;

    PaymentSuccessEventListener(LostFoundActivationService activationService) {
        this.activationService = activationService;
    }

    @EventListener
    public void handle(PaymentSucceededContext event) {
        activationService.onPaymentSucceeded(event);
    }
}
