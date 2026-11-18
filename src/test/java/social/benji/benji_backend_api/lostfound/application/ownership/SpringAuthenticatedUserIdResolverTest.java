package social.benji.benji_backend_api.lostfound.application.ownership;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import social.benji.benji_backend_api.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpringAuthenticatedUserIdResolverTest {
    private final SpringAuthenticatedUserIdResolver resolver =
            new SpringAuthenticatedUserIdResolver();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesUserPrincipalInternalIdDirectly() {
        User user = User.builder()
                .id("11111111-1111-1111-1111-111111111111")
                .email("owner@example.com")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertEquals(user.getId(), resolver.currentUserId());
    }

    @Test
    void rejectsMissingAuthentication() {
        assertThrows(IllegalStateException.class, resolver::currentUserId);
    }

    @Test
    void rejectsAnonymousAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "key", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertThrows(IllegalStateException.class, resolver::currentUserId);
    }

    @Test
    void rejectsUnsupportedPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "not-a-user", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThrows(IllegalStateException.class, resolver::currentUserId);
    }

    @Test
    void rejectsUserPrincipalWithMissingInternalId() {
        User user = User.builder().email("owner@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThrows(IllegalStateException.class, resolver::currentUserId);
    }
}
