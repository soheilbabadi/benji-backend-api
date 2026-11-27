package social.benji.benji_backend_api.identity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import social.benji.benji_backend_api.identity.dto.AuthResponse;
import social.benji.benji_backend_api.identity.dto.LoginRequest;
import social.benji.benji_backend_api.identity.dto.OtpVerificationRequest;
import social.benji.benji_backend_api.identity.dto.RegisterRequest;
import social.benji.benji_backend_api.dto.*;
import social.benji.benji_backend_api.identity.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

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
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam("file") MultipartFile file) {
        log.info("Received profile picture upload request for authenticated user: {}", principal.getUsername());
        AuthResponse response = authService.uploadProfilePicture(principal.getUsername(), file);
        return ResponseEntity.ok(response);
    }
}

