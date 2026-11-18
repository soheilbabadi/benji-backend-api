package social.benji.benji_backend_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import social.benji.benji_backend_api.controller.AuthController;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePictureSecurityTest {
    @Test
    void profilePictureEndpointUsesAuthenticatedPrincipalAndHasNoUserIdParameter() throws Exception {
        Method method = Arrays.stream(AuthController.class.getDeclaredMethods())
                .filter(candidate -> candidate.isAnnotationPresent(PostMapping.class))
                .filter(candidate -> candidate.getName().equals("uploadProfilePicture"))
                .findFirst()
                .orElseThrow();

        assertFalse(Arrays.stream(method.getParameters())
                .anyMatch(parameter -> parameter.getName().equals("userId")));
        assertTrue(Arrays.stream(method.getParameters())
                .anyMatch(parameter -> parameter.getType().equals(UserDetails.class)
                        && parameter.isAnnotationPresent(AuthenticationPrincipal.class)));
    }
}
