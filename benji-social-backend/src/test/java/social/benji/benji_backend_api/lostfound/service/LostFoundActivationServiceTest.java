package social.benji.benji_backend_api.lostfound.service;

import org.junit.jupiter.api.Test;
import social.benji.benji_backend_api.lostfound.application.payment.LostPaymentEventValidator;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementType;
import social.benji.benji_backend_api.lostfound.domain.LostFoundAdvertisementEntity;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementStatus;
import social.benji.benji_backend_api.lostfound.repository.LostFoundAdvertisementRepository;
import social.benji.benji_backend_api.payment.application.event.PaymentSucceededEvent;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LostFoundActivationServiceTest {
    @Test
    void validatesAndActivatesFromPublishedPaymentEvent() {
        LostFoundAdvertisementRepository advertisements =
                mock(LostFoundAdvertisementRepository.class);
        LostPaymentEventValidator validator = mock(LostPaymentEventValidator.class);
        UUID advertisementId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        LostFoundAdvertisementEntity advertisement = LostFoundAdvertisementEntity.builder()
                .publicId(advertisementId)
                .advertisementType(AdvertisementType.LOST)
                .status(AdvertisementStatus.PAYMENT_PENDING)
                .userId(ownerId)
                .build();
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                UUID.randomUUID(), ownerId, PaidServiceType.LOST_PET_ADVERTISEMENT,
                advertisementId, 10_000L, "IRR", "gateway-ref");
        when(advertisements.findByPublicId(advertisementId))
                .thenReturn(Optional.of(advertisement));

        new LostFoundActivationService(advertisements, validator)
                .onPaymentSucceeded(event);

        verify(validator).validate(event, advertisementId, ownerId);
        verify(advertisements).save(advertisement);
    }
}
