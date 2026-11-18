package social.benji.benji_backend_api.payment.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.benji.benji_backend_api.payment.domain.model.PaymentEventOutbox;
import social.benji.benji_backend_api.payment.domain.repository.PaymentEventOutboxRepository;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;
import social.benji.benji_backend_api.payment.domain.valueobject.PaymentEventType;
import social.benji.benji_backend_api.payment.application.event.PaymentSucceededEvent;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentOutboxProcessor {

    private final PaymentEventOutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.payment.outbox.fixed-delay-ms:5000}")
    public void processPendingEvents() {
        for (PaymentEventOutbox event : outboxRepository.findTop50ByProcessedFalseOrderByOccurredAtAsc()) {
            try {
                dispatch(event);
            } catch (Exception e) {
                markFailed(event.getId(), e);
                log.warn("Payment outbox event dispatch failed; transaction={}, retry={}",
                        event.getAggregateId(), event.getRetryCount() + 1, e);
            }
        }
    }

    @Transactional
    public void dispatch(PaymentEventOutbox event) throws Exception {
        if (event.isProcessed()) {
            return;
        }
        if (event.getEventType() == PaymentEventType.PAYMENT_SUCCEEDED) {
            String json = event.getPayload();
            PaymentSucceededPayload payload =
                    objectMapper.readValue(json, PaymentSucceededPayload.class);
            eventPublisher.publishEvent(new PaymentSucceededEvent(
                    payload.transactionId(), payload.userId(), payload.serviceType(),
                    payload.serviceReferenceId(), payload.amount(), payload.currency(),
                    payload.gatewayReferenceId()));
        }

        PaymentEventOutbox persisted = outboxRepository.findById(event.getId())
                .orElseThrow();
        persisted.markProcessed();
        outboxRepository.save(persisted);
    }

    @Transactional
    public void markFailed(UUID eventId, Exception failure) {
        outboxRepository.findById(eventId).ifPresent(event -> {
            event.markFailed(failure.getMessage());
            outboxRepository.save(event);
        });
    }

    record PaymentSucceededPayload(
            UUID transactionId,
            UUID userId,
            PaidServiceType serviceType,
            UUID serviceReferenceId,
            Long amount,
            String currency,
            String gatewayReferenceId
    ) {}
}
