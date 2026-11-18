package social.benji.benji_backend_api.payment.application.event;

import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

import java.util.UUID;

public record PaymentSucceededEvent(
        UUID transactionId,
        UUID userId,
        PaidServiceType serviceType,
        UUID serviceReferenceId,
        Long amount,
        String currency,
        String gatewayReferenceId
) {}
