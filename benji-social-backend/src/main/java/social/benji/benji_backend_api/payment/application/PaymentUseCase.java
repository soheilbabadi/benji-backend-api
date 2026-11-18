package social.benji.benji_backend_api.payment.application;

import social.benji.benji_backend_api.payment.application.dto.CreatePaymentRequest;
import social.benji.benji_backend_api.payment.application.dto.PaymentResponse;

import java.util.UUID;

public interface PaymentUseCase {
    PaymentResponse createPayment(UUID userId, CreatePaymentRequest request);
    PaymentResponse verifyCallback(String authority);
}
