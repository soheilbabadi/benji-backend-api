package social.benji.benji_backend_api.lostfound.application.payment;

import social.benji.benji_backend_api.payment.application.event.PaymentSucceededEvent;

public interface LostPaymentEventContract {
    void onPaymentSucceeded(PaymentSucceededEvent event);
}
