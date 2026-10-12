package social.benji.benji_backend_api.lostfound.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The subset of a verified payment outcome that Lost & Found needs. Deliberately a LOCAL record:
 * lostfound does not depend on any Payment entity type.
 *
 * <p>Trust rule: only events produced by the payment module's VERIFIED server-side settlement
 * (outbox relay) reach this handler — never a browser redirect, callback param, or client call.</p>
 *
 * <p>Note: the payment module stores userId as VARCHAR (users are identified by string ids in
 * this schema), so userId is carried here as String to match {@code PaymentTransactionEntity.userId}.</p>
 */
public record PaymentSucceededContext(
        UUID transactionPublicToken,
        String userId,
        UUID advertisementPublicId,   // serviceReferenceId
        BigDecimal amountIrr,
        String currency,
        Instant verifiedAt) {
}
