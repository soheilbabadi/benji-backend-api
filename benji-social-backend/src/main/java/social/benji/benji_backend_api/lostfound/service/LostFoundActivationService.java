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
import social.benji.benji_backend_api.lostfound.repository.PaymentSucceededProcessingRepository;
import social.benji.benji_backend_api.lostfound.domain.PaymentSucceededProcessingEntity;
import org.springframework.dao.DataIntegrityViolationException;
import social.benji.benji_backend_api.payment.application.event.PaymentSucceededEvent;

/**
 * The ONLY path that can turn a LOST advertisement PUBLISHED. Invoked from the payment module's
 * verified-success event (outbox relay), never from an HTTP request. Idempotent by design:
 * repeated delivery of the same success event is a no-op (approved requirement).
 */
@Service
public class LostFoundActivationService {

    private static final Logger log = LoggerFactory.getLogger(LostFoundActivationService.class);

    private final LostFoundAdvertisementRepository advertisements;
    private final LostPaymentEventValidator paymentValidator;
    private final PaymentSucceededProcessingRepository paymentProcessing;

    public LostFoundActivationService(LostFoundAdvertisementRepository advertisements,
                                      LostPaymentEventValidator paymentValidator,
                                      PaymentSucceededProcessingRepository paymentProcessing) {
        this.advertisements = advertisements;
        this.paymentValidator = paymentValidator;
        this.paymentProcessing = paymentProcessing;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        if (event == null || event.transactionId() == null
                || event.serviceReferenceId() == null) {
            log.warn("Ignoring malformed payment success event");
            return;
        }
        if (paymentProcessing.existsById(event.transactionId())) {
            return;
        }
        PaymentSucceededProcessingEntity processing;
        try {
            processing = paymentProcessing.saveAndFlush(
                    PaymentSucceededProcessingEntity.builder()
                            .transactionId(event.transactionId())
                            .advertisementPublicId(event.serviceReferenceId())
                            .build());
        } catch (DataIntegrityViolationException e) {
            return;
        }

        LostFoundAdvertisementEntity ad = advertisements
                .findByPublicIdForUpdate(event.serviceReferenceId())
                .orElse(null);
        if (ad == null) {
            // Event references an unknown ad id: stale/deleted or forged-internal — log and drop.
            log.warn("Payment success for unknown advertisement {} (txn {})",
                    event.serviceReferenceId(), event.transactionId());
            paymentProcessing.delete(processing);
            paymentProcessing.flush();
            return;
        }
        try {
            paymentValidator.validate(event, ad.getPublicId(), ad.getUserId());
            // Payer identity from the event must equal the advertiser (validated, never assumed).
            ad.activateOnVerifiedPayment(event.userId());
            advertisements.save(ad);
            processing.markProcessed(java.time.Instant.now());
            paymentProcessing.save(processing);
            log.info("Advertisement {} activated by verified payment {}",
                    ad.getPublicId(), event.transactionId());
        } catch (RuntimeException e) {
            // Duplicate event processing is recorded by transaction ID; invalid or stale events
            // are logged and not applied.
            log.warn("Ignoring payment-success for advertisement {}: {}", ad.getPublicId(), e.getMessage());
            paymentProcessing.delete(processing);
            paymentProcessing.flush();
        }
    }
}
