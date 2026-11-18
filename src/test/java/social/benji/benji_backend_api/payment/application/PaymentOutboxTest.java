package social.benji.benji_backend_api.payment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import social.benji.benji_backend_api.payment.domain.model.PaymentEventOutbox;
import social.benji.benji_backend_api.payment.domain.repository.PaymentEventOutboxRepository;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;
import social.benji.benji_backend_api.payment.domain.valueobject.PaymentEventType;
import social.benji.benji_backend_api.payment.infrastructure.event.PaymentOutboxProcessor;
import social.benji.benji_backend_api.payment.infrastructure.event.PaymentSucceededEvent;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PaymentOutboxTest {
    @Test
    void duplicateEventDispatchIsAtLeastOnceAndMarksProcessedAfterPublisherSuccess() {
        PaymentEventOutboxRepository repository = mock(PaymentEventOutboxRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ObjectMapper mapper = new ObjectMapper();
        UUID transactionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        String json = """
                {"transactionId":"%s","userId":"%s","serviceType":"LOST_PET_ADVERTISEMENT",
                 "serviceReferenceId":"%s","amount":10000,"currency":"IRR",
                 "gatewayReferenceId":"ref-1"}
                """.formatted(transactionId, userId, referenceId);
        PaymentEventOutbox event = new PaymentEventOutbox(
                PaymentEventType.PAYMENT_SUCCEEDED, transactionId, json);
        when(repository.findById(event.getId())).thenReturn(Optional.empty());

        PaymentOutboxProcessor processor =
                new PaymentOutboxProcessor(repository, publisher, mapper);

        assertThrows(Exception.class, () -> processor.dispatch(event));
        verify(publisher).publishEvent(any(PaymentSucceededEvent.class));
    }

    @Test
    void publisherFailureDoesNotMarkEventProcessed() {
        PaymentEventOutboxRepository repository = mock(PaymentEventOutboxRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        doThrow(new RuntimeException("listener failed"))
                .when(publisher).publishEvent(any(PaymentSucceededEvent.class));
        ObjectMapper mapper = new ObjectMapper();
        UUID transactionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String json = """
                {"transactionId":"%s","userId":"%s","serviceType":"EXPERT_CONSULTATION",
                 "serviceReferenceId":"%s","amount":10000,"currency":"IRR",
                 "gatewayReferenceId":"ref-1"}
                """.formatted(transactionId, userId, UUID.randomUUID());
        PaymentEventOutbox event = new PaymentEventOutbox(
                PaymentEventType.PAYMENT_SUCCEEDED, transactionId, json);

        PaymentOutboxProcessor processor =
                new PaymentOutboxProcessor(repository, publisher, mapper);

        assertThrows(RuntimeException.class, () -> processor.dispatch(event));
        verify(repository, never()).save(any(PaymentEventOutbox.class));
    }
}
