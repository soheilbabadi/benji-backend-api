package social.benji.benji_backend_api.usermanagement.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import social.benji.benji_backend_api.usermanagement.dto.AuthResponse;
import social.benji.benji_backend_api.usermanagement.dto.LoginRequest;
import social.benji.benji_backend_api.usermanagement.dto.OtpVerificationRequest;
import social.benji.benji_backend_api.usermanagement.dto.RegisterRequest;
import social.benji.benji_backend_api.usermanagement.service.LegacyAuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class LegacyAuthController {

    private final LegacyAuthService authService;

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
            @RequestParam("userId") String userId,
            @RequestParam("file") MultipartFile file) {
        log.info("Received profile picture upload request for user: {}", userId);
        AuthResponse response = authService.uploadProfilePicture(userId, file);
        return ResponseEntity.ok(response);
    }
}
