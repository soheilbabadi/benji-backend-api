package social.benji.benji_backend_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;
import social.benji.benji_backend_api.usermanagement.web.LegacyAuthController;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePictureSecurityTest {
    @Test
    void profilePictureEndpointUsesAuthenticatedPrincipalAndHasNoUserIdParameter() throws Exception {
        Method method = Arrays.stream(LegacyAuthController.class.getDeclaredMethods())
                .filter(candidate -> candidate.isAnnotationPresent(PostMapping.class))
                .filter(candidate -> candidate.getName().equals("uploadProfilePicture"))
                .findFirst()
                .orElseThrow();

        assertFalse(Arrays.stream(method.getParameters())
                .anyMatch(parameter -> parameter.getName().equals("userId")));
        assertTrue(Arrays.stream(method.getParameters())
                .anyMatch(parameter -> parameter.getType().equals(UserPrincipal.class)
                        && parameter.isAnnotationPresent(AuthenticationPrincipal.class)));
    }
}
