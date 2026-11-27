package social.benji.benji_backend_api.lostfound.application;

import org.junit.jupiter.api.Test;
import social.benji.benji_backend_api.lostfound.application.payment.LostPaymentPreparation;
import social.benji.benji_backend_api.payment.application.dto.CreatePaymentRequest;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

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

}
