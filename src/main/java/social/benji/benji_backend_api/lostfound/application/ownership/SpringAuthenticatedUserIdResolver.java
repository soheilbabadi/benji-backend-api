package social.benji.benji_backend_api.lostfound.application.ownership;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import social.benji.benji_backend_api.model.User;
import social.benji.benji_backend_api.repository.UserRepository;

@Component
public class SpringAuthenticatedUserIdResolver implements AuthenticatedUserIdResolver {
    private final UserRepository userRepository;

    public SpringAuthenticatedUserIdResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("Authentication is required");
        }

        String email;
        if (authentication.getPrincipal() instanceof UserDetails details) {
            email = details.getUsername();
        } else if (authentication.getPrincipal() instanceof User user) {
            email = user.getUsername();
        } else {
            email = authentication.getName();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user is not found"));
        return user.getId();
    }
}
