package social.benji.benji_backend_api.usermanagement.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import social.benji.benji_backend_api.usermanagement.dto.AuthResponse;
import social.benji.benji_backend_api.usermanagement.dto.LoginRequest;
import social.benji.benji_backend_api.usermanagement.dto.OtpVerificationRequest;
import social.benji.benji_backend_api.usermanagement.dto.RegisterRequest;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;
import social.benji.benji_backend_api.usermanagement.service.LegacyAuthService;
import social.benji.benji_backend_api.usermanagement.service.UserAvatarService;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class LegacyAuthController {

    private final LegacyAuthService authService;
    private final UserAvatarService userAvatarService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Login - sends OTP to user's email
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify OTP and complete authentication
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        log.info("Received OTP verification request for email: {}", request.getEmail());
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload profile picture
     */
    @PostMapping("/profile-picture")
    public ResponseEntity<AuthResponse> uploadProfilePicture(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Received profile picture upload request for authenticated user: {}", principal.id());
        var avatar = userAvatarService.upload(principal.id(), file.getContentType(), file.getBytes());
        return ResponseEntity.ok(AuthResponse.builder()
                .userId(principal.id().toString())
                .profilePictureFileId(avatar.objectKey())
                .authenticated(true)
                .message("Profile picture updated successfully")
                .build());
    }
}
