package social.benji.benji_backend_api.lostfound.application.payment;

import social.benji.benji_backend_api.payment.application.dto.CreatePaymentRequest;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

import java.util.Objects;
import java.util.UUID;

public final class LostPaymentPreparation {
    private LostPaymentPreparation() {}

    public static CreatePaymentRequest prepare(
            UUID lostAdvertisementId,
            String idempotencyKey) {
        Objects.requireNonNull(lostAdvertisementId, "Lost advertisement ID is required");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
        return new CreatePaymentRequest(
                PaidServiceType.LOST_PET_ADVERTISEMENT,
                lostAdvertisementId,
                idempotencyKey);
    }
}
