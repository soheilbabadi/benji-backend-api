package social.benji.benji_backend_api.lostfound.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementStatus;
import social.benji.benji_backend_api.lostfound.domain.LostFoundAdvertisementEntity;
import social.benji.benji_backend_api.lostfound.repository.LostFoundAdvertisementRepository;
import social.benji.benji_backend_api.payment.application.PaymentValidationUseCase;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;
import social.benji.benji_backend_api.payment.domain.valueobject.PaymentStatus;
import social.benji.benji_backend_api.payment.application.event.PaymentSucceededEvent;

import java.time.Instant;
import java.util.List;

@Component
public class LostPaymentDeadlineScheduler {
    private static final Logger log = LoggerFactory.getLogger(LostPaymentDeadlineScheduler.class);

    private final LostFoundAdvertisementRepository advertisements;
    private final PaymentValidationUseCase paymentValidation;
    private final LostFoundAdvertisementService advertisementService;

    LostPaymentDeadlineScheduler(
            LostFoundAdvertisementRepository advertisements,
            PaymentValidationUseCase paymentValidation,
            LostFoundAdvertisementService advertisementService) {
        this.advertisements = advertisements;
        this.paymentValidation = paymentValidation;
        this.advertisementService = advertisementService;
    }

    @Scheduled(fixedDelayString = "${benji.lostfound.payment-deadline-scan-ms:300000}")
    @Transactional
    public void processPaymentDeadlines() {
        Instant now = Instant.now();
        List<LostFoundAdvertisementEntity> due = advertisements
                .findByStatusAndPaymentDeadlineAtBefore(
                        AdvertisementStatus.PAYMENT_PENDING, now);
        for (LostFoundAdvertisementEntity staleAdvertisement : due) {
            LostFoundAdvertisementEntity advertisement = advertisements
                    .findByPublicIdForUpdate(staleAdvertisement.getPublicId())
                    .orElse(null);
            if (advertisement == null
                    || advertisement.getStatus() != AdvertisementStatus.PAYMENT_PENDING) {
                continue;
            }
            PaymentValidationUseCase.AuthoritativePaymentStatus payment =
                    paymentValidation.lookupLatest(
                            PaidServiceType.LOST_PET_ADVERTISEMENT,
                            advertisement.getPublicId());

            if (payment.status() == PaymentStatus.SUCCESS) {
                if (payment.userId() == null
                        || !payment.userId().equals(advertisement.getUserId())
                        || !advertisement.getPublicId().equals(payment.serviceReferenceId())
                        || !"IRR".equals(payment.currency())
                        || payment.amount() == null
                        || payment.amount() <= 0
                        || payment.gatewayReferenceId() == null
                        || payment.gatewayReferenceId().isBlank()) {
                    log.warn("Ignoring inconsistent successful payment at deadline for advertisement {}",
                            advertisement.getPublicId());
                    continue;
                }
                try {
                    paymentValidation.validateSuccessfulPayment(
                            new PaymentSucceededEvent(
                                    payment.transactionId(),
                                    payment.userId(),
                                    payment.serviceType(),
                                    payment.serviceReferenceId(),
                                    payment.amount(),
                                    payment.currency(),
                                    payment.gatewayReferenceId()),
                            PaidServiceType.LOST_PET_ADVERTISEMENT,
                            advertisement.getPublicId(),
                            advertisement.getUserId());
                    advertisement.publishLostAfterVerifiedPayment(now);
                    advertisements.save(advertisement);
                } catch (RuntimeException e) {
                    log.warn("Could not publish advertisement {} after verified payment",
                            advertisement.getPublicId(), e);
                }
            } else if (payment.status() == PaymentStatus.FAILED
                    || payment.status() == PaymentStatus.CANCELLED) {
                advertisementService.permanentDeleteFromScheduler(advertisement);
            }
            // PENDING, VERIFYING, UNKNOWN, REFUNDED remain PAYMENT_PENDING.
        }
    }
}
