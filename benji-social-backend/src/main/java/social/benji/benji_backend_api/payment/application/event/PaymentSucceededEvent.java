package social.benji.benji_backend_api.payment.application.event;

import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

import java.util.UUID;

/**
 * Application-level representation of the Payment module's successful outbox event.
 * transactionId is the stable event/payment identity for future consumer deduplication.
 * Consumers must not infer success or ownership from this event alone; they must
 * validate it against the authoritative Payment transaction.
 */
public record PaymentSucceededEvent(
        UUID transactionId,
        UUID userId,
        PaidServiceType serviceType,
        UUID serviceReferenceId,
        Long amount,
        String currency,
        String gatewayReferenceId
) {}
