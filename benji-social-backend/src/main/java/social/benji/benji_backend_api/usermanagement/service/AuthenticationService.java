package social.benji.benji_backend_api.usermanagement.service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.domain.UserStatus;
import social.benji.benji_backend_api.usermanagement.domain.exception.UserManagementErrors;
import social.benji.benji_backend_api.usermanagement.security.JwtTokenProvider;

/**
 * Orchestrates the login use case: OTP verification -> account resolution or
 * creation -> token issuance. User creation happens only AFTER a code is
 * verified, so unverified numbers can never create rows (no phantom-account
 * DoS, no enumeration side effects).
 */
@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final OtpService otpService;
    private final UserService userService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MobileNumberNormalizer normalizer;
    private final Clock clock;

    public AuthenticationService(OtpService otpService,
                                 UserService userService,
                                 TokenService tokenService,
                                 JwtTokenProvider jwtTokenProvider,
                                 MobileNumberNormalizer normalizer,
                                 Clock clock) {
        this.otpService = otpService;
        this.userService = userService;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.normalizer = normalizer;
        this.clock = clock;
    }

    public void requestOtp(String rawMobile) {
        otpService.requestOtp(normalizer.normalize(rawMobile));
    }

    @Transactional
    public AuthTokens verifyAndAuthenticate(String rawMobile, String code) {
        String mobile = normalizer.normalize(rawMobile);
        otpService.verifyOtp(mobile, code);

        User user = userService.findByMobile(mobile)
                .orElseGet(() -> userService.registerWithVerifiedMobile(mobile));

        if (user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.SUSPENDED) {
            log.warn("Blocked/suspended sign-in attempt for userId={}", user.getId());
            throw UserManagementErrors.userBlocked();
        }
        if (!user.isActive()) {
            // Deleted-but-released identity: treat as a fresh registration path is
            // impossible here because deletedAt rows are excluded from lookup,
            // so reaching this branch means a race — fail closed.
            throw UserManagementErrors.authenticationFailed();
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getMobileNumber(),
                List.of("ROLE_" + user.getRole().name()), user.isMobileVerified());
        String refreshToken = tokenService.issue(user.getId());

        log.info("Authentication succeeded userId={} isNewUser={}",
                user.getId(), user.getCreatedAt().isAfter(clock.instant().minusSeconds(5)));
        return new AuthTokens(accessToken, refreshToken,
                jwtTokenProvider.accessTokenTtl().toSeconds(), user.getId());
    }

    @Transactional
    public AuthTokens refresh(String refreshToken) {
        TokenService.RefreshResult result = tokenService.refresh(refreshToken);
        User user = userService.findById(result.userId());
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getMobileNumber(),
                List.of("ROLE_" + user.getRole().name()), user.isMobileVerified());
        return new AuthTokens(accessToken, result.newRawToken(),
                jwtTokenProvider.accessTokenTtl().toSeconds(), user.getId());
    }

    /**
     * Single-session logout: revokes the presented refresh token so it can no
     * longer mint access tokens. Idempotent — an unknown/already-revoked token
     * is a silent no-op (revocation state is not leaked to the caller). The
     * short-lived access token remains valid until natural expiry; clients
     * should discard it immediately.
     */
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw UserManagementErrors.invalidRefreshToken();
        }
        tokenService.revoke(refreshToken);
        log.info("Logout: refresh token revoked");
    }

    /**
     * Global logout ("sign out everywhere"): revokes every live refresh session
     * of the authenticated user. Requires a valid access token, so the caller
     * identity comes from the SecurityContext, never from the request body.
     */
    @Transactional
    public void logoutAll(UUID userId) {
        tokenService.revokeAllForUser(userId);
        log.info("Logout-all: all refresh sessions revoked for userId={}", userId);
    }

    public record AuthTokens(String accessToken, String refreshToken, long expiresInSeconds, UUID userId) {
    }
}
