package social.benji.benji_backend_api.usermanagement.security;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.repository.UserRepository;
import social.benji.benji_backend_api.usermanagement.service.MobileNumberNormalizer;

/**
 * Validates the bearer access token and rebuilds a fresh principal from the
 * current database row, so status changes (BLOCKED/DELETED) take effect without
 * waiting for the short-lived JWT to expire. Never trusts stale JWT claims for
 * authorization decisions.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, java.io.IOException {
        extractToken(request).ifPresent(token -> {
            String subject = tokenProvider.verify(token);
            if (subject != null) {
                resolvePrincipal(subject).ifPresent(principal -> SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(
                                principal, null, principal.authorities())));
            }
        });
        filterChain.doFilter(request, response);
    }

    private Optional<UserPrincipal> resolvePrincipal(String subject) {
        try {
            UUID userId = UUID.fromString(subject);
            return userRepository.findById(userId)
                    .filter(User::isActive)
                    .map(u -> new UserPrincipal(
                            u.getId(),
                            u.getMobileNumber(),
                            List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String value = header.substring(BEARER_PREFIX.length()).trim();
            return value.isEmpty() ? Optional.empty() : Optional.of(value);
        }
        return Optional.empty();
    }

    /** Minimal authenticated identity carried in the security context. */
    public record UserPrincipal(UUID id, String maskedMobile,
                                List<SimpleGrantedAuthority> authorities) {

        public UserPrincipal(UUID id, String mobile, List<SimpleGrantedAuthority> authorities) {
            this(id, MobileNumberNormalizer.mask(mobile), authorities);
        }
    }
}
