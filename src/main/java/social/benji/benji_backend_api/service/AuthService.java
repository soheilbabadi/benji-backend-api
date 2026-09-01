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
import java.util.Random;
import java.util.Set;

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

    /**
     * Registers a new user and sends OTP to their email.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Optional<User> existingUserByMobile = userRepository.findByMobileNumber(request.getMobileNumber());
        if (existingUserByMobile.isPresent()) {
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
    public AuthResponse uploadProfilePicture(String userId, MultipartFile file) {
        log.info("Uploading profile picture for user: {}", userId);

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete old profile picture if exists
        if (user.getProfilePictureFileId() != null) {
            objectStorageService.deleteFile(user.getProfilePictureFileId(), "profile-pictures");
        }

        // Upload new file
        try {
            String fileId = objectStorageService.uploadFile(file, "profile-pictures");
            user.setProfilePictureFileId(fileId);
            userRepository.save(user);

            return AuthResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .profilePictureFileId(fileId)
                    .authenticated(true)
                    .message("Profile picture updated successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error uploading profile picture", e);
            throw new RuntimeException("Failed to upload profile picture: " + e.getMessage());
        }
    }

    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
