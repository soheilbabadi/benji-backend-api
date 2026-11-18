package social.benji.benji_backend_api.payment.application;

import java.util.UUID;

public interface PaymentGateway {
    GatewayRequestResult requestPayment(UUID transactionId, long amountRials);
    GatewayVerificationResult verifyPayment(String authority, long amountRials);

    record GatewayRequestResult(String authority, String paymentUrl) {}
    record GatewayVerificationResult(boolean successful, String referenceId, String failureReason) {}
}
