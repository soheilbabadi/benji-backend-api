package social.benji.benji_backend_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import social.benji.benji_backend_api.dto.*;
import social.benji.benji_backend_api.model.Role;
import social.benji.benji_backend_api.model.User;
import social.benji.benji_backend_api.repository.RoleRepository;
import social.benji.benji_backend_api.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final ObjectStorageService objectStorageService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Registers a new user and sends OTP to their email.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new RuntimeException("Mobile number already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(false)
                .otpVerified(false)
                .build();

        // Assign default USER role
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));
        roles.add(userRole);
        user.setRoles(roles);

        // Generate and send OTP
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        emailService.sendOtpEmail(savedUser.getEmail(), otp);

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .mobileNumber(savedUser.getMobileNumber())
                .authenticated(false)
                .message("Registration successful. Please verify your email with the OTP sent.")
                .build();
    }

    /**
     * Initiates login by sending OTP to the user's email.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        if (!user.isEnabled()) {
            throw new RuntimeException("User account is not activated. Please complete registration.");
        }

        // Generate and send OTP
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now());
        user.setOtpVerified(false);

        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .authenticated(false)
                .message("OTP sent to your email. Please verify to complete login.")
                .build();
    }

    /**
     * Verifies OTP and completes authentication.
     */
    @Transactional
    public AuthResponse verifyOtp(OtpVerificationRequest request) {
        log.info("Verifying OTP for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        LocalDateTime otpGeneratedAt = user.getOtpGeneratedAt();
        if (otpGeneratedAt == null || 
            otpGeneratedAt.isBefore(LocalDateTime.now().minusMinutes(OTP_VALIDITY_MINUTES))) {
            throw new RuntimeException("OTP has expired");
        }

        user.setOtpVerified(true);
        user.setOtp(null);
        user.setOtpGeneratedAt(null);
        user.setEnabled(true);

        userRepository.save(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePictureFileId(user.getProfilePictureFileId())
                .authenticated(true)
                .message("Login successful")
                .build();
    }

    /**
     * Uploads profile picture and updates user record.
     */
    @Transactional
    public AuthResponse uploadProfilePicture(String authenticatedEmail, MultipartFile file) {
        log.info("Uploading profile picture for authenticated user: {}", authenticatedEmail);

        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        String oldObjectKey = user.getProfilePictureFileId();
        UploadedObjectMetadata uploaded;
        try {
            uploaded = objectStorageService.upload(file, "profile-pictures");
        } catch (Exception e) {
            log.error("Profile picture upload failed for {}", authenticatedEmail, e);
            throw new RuntimeException("Failed to upload profile picture", e);
        }

        try {
            user.setProfilePictureFileId(uploaded.objectKey());
            userRepository.saveAndFlush(user);
        } catch (Exception e) {
            log.error("Database persistence failed after uploading profile picture {}", uploaded.objectKey(), e);
            try {
                objectStorageService.delete(uploaded.objectKey());
            } catch (Exception cleanupException) {
                log.error("Failed to clean up newly uploaded profile picture {}", uploaded.objectKey(), cleanupException);
            }
            throw new RuntimeException("Failed to persist profile picture", e);
        }

        if (oldObjectKey != null && !oldObjectKey.equals(uploaded.objectKey())) {
            try {
                objectStorageService.delete(oldObjectKey);
            } catch (Exception e) {
                log.warn("New profile picture saved but old object cleanup failed: {}", oldObjectKey, e);
            }
        }

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .profilePictureFileId(uploaded.objectKey())
                .authenticated(true)
                .message("Profile picture updated successfully")
                .build();
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(SECURE_RANDOM.nextInt(10));
        }
        return otp.toString();
    }
}
