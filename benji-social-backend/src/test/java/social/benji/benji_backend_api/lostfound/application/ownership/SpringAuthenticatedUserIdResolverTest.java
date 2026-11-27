package social.benji.benji_backend_api.lostfound.application.ownership;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;
import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.model.LegacyUser;

import java.util.List;
import java.util.UUID;

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
        UserPrincipal user = new UserPrincipal(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "+98***1234",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertEquals(user.id().toString(), resolver.currentUserId());
    }

    @Test
    void resolvesCanonicalUserEntityIdDirectly() {
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        User user = User.builder().id(id).mobileNumber("+989121234567").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertEquals(id.toString(), resolver.currentUserId());
    }

    @Test
    void resolvesLegacyUserDetailsIdDirectly() {
        String id = "33333333-3333-3333-3333-333333333333";
        LegacyUser user = LegacyUser.builder().id(id).email("legacy@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertEquals(id, resolver.currentUserId());
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
        UserPrincipal user = new UserPrincipal(
                null,
                "+98***1234",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThrows(IllegalStateException.class, resolver::currentUserId);
    }
}
