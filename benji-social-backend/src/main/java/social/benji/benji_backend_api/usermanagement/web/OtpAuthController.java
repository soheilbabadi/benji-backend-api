package social.benji.benji_backend_api.usermanagement.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import social.benji.benji_backend_api.usermanagement.domain.exception.UserManagementErrors;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;
import social.benji.benji_backend_api.usermanagement.service.AuthenticationService;
import social.benji.benji_backend_api.usermanagement.web.dto.OtpRequest;
import social.benji.benji_backend_api.usermanagement.web.dto.OtpVerifyRequest;

/**
 * Public entry point of the sign-up/sign-in cycle. Lives under {@code /api/auth/**}
 * which SecurityConfig already permits anonymously, so no token is needed here.
 *
 * <p>Flow: {@code POST otp} asks the SmsSender port — backed by the Kavenegar
 * API in production — to deliver a one-time code; {@code POST otp/verify}
 * consumes the code, creates the account on first use (role USER + default
 * picsum avatar) and returns JWT tokens.
 */
@RestController
@RequestMapping("/api/auth/otp")
public class OtpAuthController {

    private final AuthenticationService authenticationService;

    public OtpAuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /** Request (or resend, subject to the service-side cooldown) an OTP via Kavenegar SMS. */
    @PostMapping
    public ResponseEntity<Void> requestOtp(@Valid @RequestBody OtpRequest request) {
        authenticationService.requestOtp(request.mobile());
        return ResponseEntity.accepted().build(); // 202 — delivery is async from the client's view
    }

    /** Verify the OTP; on success returns tokens (and seeds a new account if needed). */
    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody OtpVerifyRequest request) {
        AuthenticationService.AuthTokens tokens =
                authenticationService.verifyAndAuthenticate(request.mobile(), request.code());
        return ResponseEntity.status(HttpStatus.OK).body(new AuthResponse(
                tokens.accessToken(), tokens.refreshToken(), tokens.expiresInSeconds(), tokens.userId()));
    }

    /** Refresh an access token using a valid refresh token. */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        AuthenticationService.AuthTokens tokens = authenticationService.refresh(request.refreshToken());
        return ResponseEntity.ok(new AuthResponse(
                tokens.accessToken(), tokens.refreshToken(), tokens.expiresInSeconds(), tokens.userId()));
    }

    /**
     * Logout (single session): revokes the presented refresh token so it can no
     * longer be exchanged for new access tokens. Idempotent — always 204, even
     * if the token is unknown or already revoked (no state leakage). The caller
     * should also discard the access token client-side; it expires naturally.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request == null ? null : request.refreshToken());
        return ResponseEntity.noContent().build(); // 204
    }

    /**
     * Global logout ("sign out of all devices"): requires a valid Bearer access
     * token and revokes every live refresh session of the authenticated user.
     * User identity is taken from the SecurityContext, never from the body.
     */
    @PostMapping("/logout/all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw UserManagementErrors.authenticationFailed();
        }
        authenticationService.logoutAll(principal.id());
        return ResponseEntity.noContent().build(); // 204
    }

    public record AuthResponse(String accessToken, String refreshToken,
                               long expiresInSeconds, java.util.UUID userId) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record LogoutRequest(String refreshToken) {
    }
}
