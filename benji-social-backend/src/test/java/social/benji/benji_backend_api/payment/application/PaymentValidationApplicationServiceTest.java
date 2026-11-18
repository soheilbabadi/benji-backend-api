package social.benji.benji_backend_api.payment.application;

import org.junit.jupiter.api.Test;
import social.benji.benji_backend_api.payment.application.event.PaymentSucceededEvent;
import social.benji.benji_backend_api.payment.domain.model.PaymentTransaction;
import social.benji.benji_backend_api.payment.domain.repository.PaymentTransactionRepository;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;
import social.benji.benji_backend_api.payment.domain.valueobject.PaymentStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentValidationApplicationServiceTest {
    private final UUID transactionId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();
    private final UUID advertisementId = UUID.randomUUID();

    @Test
    void rejectsMissingTransaction() {
        PaymentTransactionRepository repository = mock(PaymentTransactionRepository.class);
        when(repository.findById(transactionId)).thenReturn(Optional.empty());
        PaymentValidationApplicationService service =
                new PaymentValidationApplicationService(repository);
        PaymentSucceededEvent event = successfulEvent();

        assertThrows(IllegalArgumentException.class,
                () -> service.validateSuccessfulPayment(
                        event, PaidServiceType.LOST_PET_ADVERTISEMENT,
                        advertisementId, ownerId));
    }

    @Test
    void acceptsSuccessfulAuthoritativeTransaction() {
        PaymentTransactionRepository repository = mock(PaymentTransactionRepository.class);
        PaymentTransaction transaction = mock(PaymentTransaction.class);
        when(repository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transaction.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(transaction.getId()).thenReturn(transactionId);
        when(transaction.getUserId()).thenReturn(ownerId);
        when(transaction.getServiceType()).thenReturn(PaidServiceType.LOST_PET_ADVERTISEMENT);
        when(transaction.getServiceReferenceId()).thenReturn(advertisementId);
        when(transaction.getAmount()).thenReturn(10000L);
        when(transaction.getCurrency()).thenReturn("IRR");
        when(transaction.getGatewayReferenceId()).thenReturn("gateway-ref");

        PaymentValidationApplicationService service =
                new PaymentValidationApplicationService(repository);
        PaymentValidationUseCase.ValidatedPaymentResult result =
                service.validateSuccessfulPayment(
                        successfulEvent(),
                        PaidServiceType.LOST_PET_ADVERTISEMENT,
                        advertisementId,
                        ownerId);

        assertEquals(transactionId, result.transactionId());
        assertEquals(PaymentStatus.SUCCESS, result.status());
    }

    @Test
    void rejectsTransactionNotSuccessful() {
        PaymentTransactionRepository repository = mock(PaymentTransactionRepository.class);
        PaymentTransaction transaction = mock(PaymentTransaction.class);
        when(repository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transaction.getStatus()).thenReturn(PaymentStatus.PENDING);

        PaymentValidationApplicationService service =
                new PaymentValidationApplicationService(repository);

        assertThrows(IllegalArgumentException.class,
                () -> service.validateSuccessfulPayment(
                        successfulEvent(), PaidServiceType.LOST_PET_ADVERTISEMENT,
                        advertisementId, ownerId));
    }

    @Test
    void rejectsWrongServiceType() {
        assertRejected(t -> {
            when(t.getStatus()).thenReturn(PaymentStatus.SUCCESS);
            when(t.getServiceType()).thenReturn(PaidServiceType.EXPERT_CONSULTATION);
        });
    }

    @Test
    void rejectsWrongAdvertisementReference() {
        assertRejected(t -> {
            when(t.getStatus()).thenReturn(PaymentStatus.SUCCESS);
            when(t.getServiceType()).thenReturn(PaidServiceType.LOST_PET_ADVERTISEMENT);
            when(t.getServiceReferenceId()).thenReturn(UUID.randomUUID());
        });
    }

    @Test
    void rejectsWrongOwner() {
        assertRejected(t -> {
            when(t.getStatus()).thenReturn(PaymentStatus.SUCCESS);
            when(t.getServiceType()).thenReturn(PaidServiceType.LOST_PET_ADVERTISEMENT);
            when(t.getServiceReferenceId()).thenReturn(advertisementId);
            when(t.getUserId()).thenReturn(UUID.randomUUID());
        });
    }

    @Test
    void rejectsWrongCurrency() {
        assertRejected(t -> {
            when(t.getStatus()).thenReturn(PaymentStatus.SUCCESS);
            when(t.getServiceType()).thenReturn(PaidServiceType.LOST_PET_ADVERTISEMENT);
            when(t.getServiceReferenceId()).thenReturn(advertisementId);
            when(t.getUserId()).thenReturn(ownerId);
            when(t.getCurrency()).thenReturn("USD");
        });
    }

    @Test
    void rejectsWrongAmount() {
        assertRejected(t -> {
            when(t.getStatus()).thenReturn(PaymentStatus.SUCCESS);
            when(t.getServiceType()).thenReturn(PaidServiceType.LOST_PET_ADVERTISEMENT);
            when(t.getServiceReferenceId()).thenReturn(advertisementId);
            when(t.getUserId()).thenReturn(ownerId);
            when(t.getCurrency()).thenReturn("IRR");
            when(t.getAmount()).thenReturn(9000L);
        });
    }

    @Test
    void rejectsMalformedEventAndGatewayReferenceMismatch() {
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentValidationApplicationService(
                        mock(PaymentTransactionRepository.class))
                        .validateSuccessfulPayment(
                                null,
                                PaidServiceType.LOST_PET_ADVERTISEMENT,
                                advertisementId,
                                ownerId));

        PaymentTransactionRepository repository = mock(PaymentTransactionRepository.class);
        PaymentTransaction transaction = mock(PaymentTransaction.class);
        when(repository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transaction.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(transaction.getServiceType()).thenReturn(PaidServiceType.LOST_PET_ADVERTISEMENT);
        when(transaction.getServiceReferenceId()).thenReturn(advertisementId);
        when(transaction.getUserId()).thenReturn(ownerId);
        when(transaction.getCurrency()).thenReturn("IRR");
        when(transaction.getAmount()).thenReturn(10000L);
        when(transaction.getGatewayReferenceId()).thenReturn("different-ref");

        assertThrows(IllegalArgumentException.class,
                () -> new PaymentValidationApplicationService(repository)
                        .validateSuccessfulPayment(
                                successfulEvent(),
                                PaidServiceType.LOST_PET_ADVERTISEMENT,
                                advertisementId,
                                ownerId));
    }

    @Test
    void rejectsInconsistentTransactionId() {
        PaymentTransactionRepository repository = mock(PaymentTransactionRepository.class);
        PaymentTransaction transaction = mock(PaymentTransaction.class);
        when(repository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transaction.getId()).thenReturn(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class,
                () -> new PaymentValidationApplicationService(repository)
                                .validateSuccessfulPayment(
                                        successfulEvent(),
                                        PaidServiceType.LOST_PET_ADVERTISEMENT,
                                        advertisementId,
                                        ownerId));
    }

    private PaymentSucceededEvent successfulEvent() {
        return new PaymentSucceededEvent(
                transactionId, ownerId,
                PaidServiceType.LOST_PET_ADVERTISEMENT,
                advertisementId, 10000L, "IRR", "gateway-ref");
    }

    private void assertRejected(java.util.function.Consumer<PaymentTransaction> configure) {
        PaymentTransactionRepository repository = mock(PaymentTransactionRepository.class);
        PaymentTransaction transaction = mock(PaymentTransaction.class);
        configure.accept(transaction);
        when(repository.findById(transactionId)).thenReturn(Optional.of(transaction));

        assertThrows(IllegalArgumentException.class,
                () -> new PaymentValidationApplicationService(repository)
                        .validateSuccessfulPayment(
                                successfulEvent(),
                                PaidServiceType.LOST_PET_ADVERTISEMENT,
                                advertisementId,
                                ownerId));
    }
}
