package social.benji.benji_backend_api.lostfound.application.ownership;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.model.LegacyUser;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

@Component
public class SpringAuthenticatedUserIdResolver implements AuthenticatedUserIdResolver {
    @Override
    public String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal user) {
            return requireInternalId(user.id() == null ? null : user.id().toString(),
                    "Authenticated UserPrincipal");
        }
        if (principal instanceof User user) {
            return requireInternalId(user.getId() == null ? null : user.getId().toString(),
                    "Authenticated User");
        }
        if (principal instanceof LegacyUser user) {
            return requireInternalId(user.getId(), "Authenticated LegacyUser");
        }

        throw new IllegalStateException("Unsupported authenticated principal");
    }

    private String requireInternalId(String id, String principalType) {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException(principalType + " has no internal ID");
        }
        return id;
    }
}
