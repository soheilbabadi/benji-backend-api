package social.benji.benji_backend_api.lostfound.domain.lifecycle;

import java.util.Set;

public enum FoundAdvertisementLifecycle {
    UNDER_REVIEW,
    REJECTED,
    PUBLISHED,
    EXPIRED,
    DEACTIVATED;

    public boolean isPubliclyVisible() {
        return this == PUBLISHED;
    }

    public boolean isOwnerEditable() {
        return this == REJECTED;
    }

    public boolean canTransitionTo(FoundAdvertisementLifecycle next) {
        if (next == null || next == this) {
            return false;
        }
        return switch (this) {
            case UNDER_REVIEW -> Set.of(REJECTED, PUBLISHED).contains(next);
            case REJECTED -> next == UNDER_REVIEW;
            case PUBLISHED -> Set.of(EXPIRED, DEACTIVATED).contains(next);
            case EXPIRED, DEACTIVATED -> false;
        };
    }
}
