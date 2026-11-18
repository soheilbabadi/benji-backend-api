package social.benji.benji_backend_api.lostfound.application;

import org.junit.jupiter.api.Test;
import social.benji.benji_backend_api.lostfound.application.payment.LostPaymentPreparation;
import social.benji.benji_backend_api.lostfound.application.payment.LostPaymentEventValidator;
import social.benji.benji_backend_api.payment.application.dto.CreatePaymentRequest;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;
import social.benji.benji_backend_api.payment.infrastructure.event.PaymentSucceededEvent;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LostPaymentPreparationTest {
    @Test
    void preparesLostPaymentWithAdvertisementAsServiceReference() {
        UUID advertisementId = UUID.randomUUID();
        CreatePaymentRequest request =
                LostPaymentPreparation.prepare(advertisementId, "idem-1");

        assertEquals(PaidServiceType.LOST_PET_ADVERTISEMENT, request.serviceType());
        assertEquals(advertisementId, request.serviceReferenceId());
        assertEquals("idem-1", request.idempotencyKey());
    }

    @Test
    void rejectsWrongServiceTypeAndDifferentAdvertisementEvent() {
        UUID advertisementId = UUID.randomUUID();
        PaymentSucceededEvent wrongType = new PaymentSucceededEvent(
                UUID.randomUUID(), UUID.randomUUID(),
                PaidServiceType.EXPERT_CONSULTATION, advertisementId,
                10000L, "IRR", "gateway-ref");
        assertThrows(IllegalArgumentException.class,
                () -> LostPaymentEventValidator.validate(wrongType, advertisementId));

        PaymentSucceededEvent wrongReference = new PaymentSucceededEvent(
                UUID.randomUUID(), UUID.randomUUID(),
                PaidServiceType.LOST_PET_ADVERTISEMENT, UUID.randomUUID(),
                10000L, "IRR", "gateway-ref");
        assertThrows(IllegalArgumentException.class,
                () -> LostPaymentEventValidator.validate(wrongReference, advertisementId));
    }
}
