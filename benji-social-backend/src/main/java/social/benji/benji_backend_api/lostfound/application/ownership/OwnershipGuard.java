package social.benji.benji_backend_api.lostfound.application.ownership;

import java.util.Objects;

public final class OwnershipGuard {
    private OwnershipGuard() {}

    public static void requireOwner(String ownerId, String authenticatedUserId) {
        if (ownerId == null || authenticatedUserId == null
                || !Objects.equals(ownerId, authenticatedUserId)) {
            throw new IllegalArgumentException("The authenticated user does not own this advertisement");
        }
    }
}
