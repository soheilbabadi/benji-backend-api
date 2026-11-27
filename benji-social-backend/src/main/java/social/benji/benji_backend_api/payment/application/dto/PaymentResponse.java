package social.benji.benji_backend_api.payment.application.dto;

import social.benji.benji_backend_api.payment.domain.valueobject.PaymentStatus;

import java.util.UUID;

public record PaymentResponse(
        UUID transactionId,
        PaymentStatus status,
        Long amount,
        String currency,
        String gatewayAuthority,
        String gatewayUrl
) {}
