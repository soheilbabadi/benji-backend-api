package social.benji.benji_backend_api.lostfound.application.ownership;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import social.benji.benji_backend_api.model.User;

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
        if (principal instanceof User user) {
            if (user.getId() == null || user.getId().isBlank()) {
                throw new IllegalStateException("Authenticated user has no internal ID");
            }
            return user.getId();
        }

        throw new IllegalStateException("Unsupported authenticated principal");
    }
}
