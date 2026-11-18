package social.benji.benji_backend_api.lostfound.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.lostfound.application.payment.LostPaymentEventValidator;
import social.benji.benji_backend_api.lostfound.domain.LostFoundAdvertisementEntity;
import social.benji.benji_backend_api.lostfound.repository.LostFoundAdvertisementRepository;
import social.benji.benji_backend_api.payment.application.event.PaymentSucceededEvent;

/**
 * The ONLY path that can turn a LOST advertisement ACTIVE. Invoked from the payment module's
 * verified-success event (outbox relay), never from an HTTP request. Idempotent by design:
 * repeated delivery of the same success event is a no-op (approved requirement).
 */
@Service
public class LostFoundActivationService {

    private static final Logger log = LoggerFactory.getLogger(LostFoundActivationService.class);

    private final LostFoundAdvertisementRepository advertisements;
    private final LostPaymentEventValidator paymentValidator;

    public LostFoundActivationService(LostFoundAdvertisementRepository advertisements,
                                      LostPaymentEventValidator paymentValidator) {
        this.advertisements = advertisements;
        this.paymentValidator = paymentValidator;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        LostFoundAdvertisementEntity ad = advertisements
                .findByPublicId(event.serviceReferenceId())
                .orElse(null);
        if (ad == null) {
            // Event references an unknown ad id: stale/deleted or forged-internal — log and drop.
            log.warn("Payment success for unknown advertisement {} (txn {})",
                    event.serviceReferenceId(), event.transactionId());
            return;
        }
        try {
            paymentValidator.validate(event, ad.getPublicId(), ad.getUserId());
            // Payer identity from the event must equal the advertiser (validated, never assumed).
            ad.activateOnVerifiedPayment(event.userId());
            advertisements.save(ad);
            log.info("Advertisement {} activated by verified payment {}",
                    ad.getPublicId(), event.transactionId());
        } catch (BenjiCustomException e) {
            // Duplicate event after ACTIVE is handled inside the entity as a no-op; anything else
            // (e.g. success arriving after CANCELLED) is logged for refund review, NOT applied.
            log.warn("Ignoring payment-success for advertisement {}: {}", ad.getPublicId(), e.getMessage());
        }
    }
}
