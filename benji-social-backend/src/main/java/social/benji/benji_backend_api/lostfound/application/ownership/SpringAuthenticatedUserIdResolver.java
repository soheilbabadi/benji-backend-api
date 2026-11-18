package social.benji.benji_backend_api.lostfound.application.ownership;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
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
            if (user.id() == null) {
                throw new IllegalStateException("Authenticated user has no internal ID");
            }
            return user.id().toString();
        }

        throw new IllegalStateException("Unsupported authenticated principal");
    }
}
