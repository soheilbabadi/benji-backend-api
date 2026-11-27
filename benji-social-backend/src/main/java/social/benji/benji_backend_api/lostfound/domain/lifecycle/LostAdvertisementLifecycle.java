package social.benji.benji_backend_api.lostfound.domain.lifecycle;

import java.util.Set;

public enum LostAdvertisementLifecycle {
    UNDER_REVIEW,
    REJECTED,
    PAYMENT_PENDING,
    PUBLISHED,
    EXPIRED,
    DEACTIVATED;

    public boolean isPubliclyVisible() {
        return this == PUBLISHED;
    }

    public boolean isOwnerEditable() {
        return this == REJECTED;
    }

    public boolean canTransitionTo(LostAdvertisementLifecycle next) {
        if (next == null || next == this) {
            return false;
        }
        return switch (this) {
            case UNDER_REVIEW -> Set.of(REJECTED, PAYMENT_PENDING).contains(next);
            case REJECTED -> next == UNDER_REVIEW;
            case PAYMENT_PENDING -> next == PUBLISHED;
            case PUBLISHED -> Set.of(EXPIRED, DEACTIVATED).contains(next);
            case EXPIRED, DEACTIVATED -> false;
        };
    }
}
