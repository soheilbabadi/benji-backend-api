package social.benji.benji_backend_api.lostfound.domain;

import java.util.Set;

/**
 * Advertisement lifecycle. Owned exclusively by Lost &amp; Found — deliberately NOT the payment
 * state machine (Payment owns {@code PaymentStatus}; an ad only reacts to verified outcomes).
 *
 * <h2>State machine (approved)</h2>
 * <pre>
 *  LOST:  DRAFT -> PAYMENT_PENDING -> ACTIVE -> RESOLVED | EXPIRED | CANCELLED(pre-activation) | CLOSED | HIDDEN
 *  FOUND: DRAFT -> ACTIVE -> RESOLVED | EXPIRED | CLOSED | HIDDEN   (never requires payment)
 * </pre>
 *
 * Key confirmed rules encoded in {@link #canTransitionTo}:
 * <ul>
 *   <li><b>RESOLVED is terminal</b> — a resolved ad can NEVER be reactivated; users must create
 *       a new advertisement (approved decision).</li>
 *   <li>ACTIVE -&gt; PAYMENT_PENDING is impossible: activation happens ONLY via a server-side
 *       verified payment event, never via a client call or browser redirect.</li>
 *   <li>PAYMENT_PENDING -&gt; ACTIVE only when the payment module reports SUCCESS for this ad.</li>
 *   <li>HIDDEN (moderation) can come from any visible state and returns only to ACTIVE via admin restore.</li>
 * </ul>
 */
public enum AdvertisementStatus {

    DRAFT,
    PAYMENT_PENDING,
    ACTIVE,
    RESOLVED,
    EXPIRED,
    CANCELLED,
    CLOSED,
    HIDDEN;

    /** States that appear on public listings/search. */
    public boolean isPubliclyVisible() {
        return this == ACTIVE || this == RESOLVED || this == EXPIRED;
    }

    /** States from which the advertiser may still edit content. */
    public boolean isEditable() {
        return this == DRAFT || this == PAYMENT_PENDING;
    }

    public boolean canTransitionTo(AdvertisementStatus next) {
        if (next == null || next == this) {
            return false;
        }
        return switch (this) {
            case DRAFT -> Set.of(PAYMENT_PENDING, ACTIVE, CANCELLED).contains(next);
            // Awaiting a verified payment outcome; failure/cancel keeps the ad here so the user
            // can start a NEW payment attempt (attempts are separate payment transactions).
            case PAYMENT_PENDING -> Set.of(ACTIVE, CANCELLED, CLOSED).contains(next);
            case ACTIVE -> Set.of(RESOLVED, EXPIRED, CLOSED, HIDDEN).contains(next);
            // Approved: resolved ads cannot be reactivated — no transition out of RESOLVED.
            case RESOLVED -> Set.of(CLOSED).contains(next);
            case EXPIRED -> Set.of(CLOSED, ACTIVE).contains(next); // renewal = republish path
            case CANCELLED, CLOSED -> false;
            case HIDDEN -> Set.of(ACTIVE, CLOSED).contains(next);
        };
    }
}
