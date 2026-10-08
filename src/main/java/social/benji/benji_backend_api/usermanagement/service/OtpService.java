package social.benji.benji_backend_api.usermanagement.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import social.benji.benji_backend_api.usermanagement.config.OtpProperties;
import social.benji.benji_backend_api.usermanagement.domain.OtpChallenge;
import social.benji.benji_backend_api.usermanagement.domain.OtpChallengeStatus;
import social.benji.benji_backend_api.usermanagement.domain.exception.UserManagementErrors;
import social.benji.benji_backend_api.usermanagement.port.SmsSender;
import social.benji.benji_backend_api.usermanagement.repository.OtpChallengeRepository;

/**
 * Owns the OTP challenge lifecycle: request, resend throttling, verification,
 * attempt accounting and replay prevention. Knows nothing about SMS providers
 * beyond the {@link SmsSender} port and never touches the User entity.
 */
@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_BOUND = 1_000_000;

    private final OtpChallengeRepository challengeRepository;
    private final SmsSender smsSender;
    private final OtpProperties properties;
    private final SecureRandom random;
    private final Clock clock;

    public OtpService(OtpChallengeRepository challengeRepository,
                      SmsSender smsSender,
                      OtpProperties properties,
                      SecureRandom secureRandom,
                      Clock clock) {
        this.challengeRepository = challengeRepository;
        this.smsSender = smsSender;
        this.properties = properties;
        this.random = secureRandom;
        this.clock = clock;
    }

    @Transactional
    public void requestOtp(String normalizedMobile) {
        Instant now = clock.instant();

        // Cooldown is measured from the most recent request of any state, so an
        // expired/blocked challenge cannot be used to bypass the resend throttle.
        Instant lastRequestAt = challengeRepository.findLatestCreatedAt(normalizedMobile).orElse(null);
        if (lastRequestAt != null
                && now.isBefore(lastRequestAt.plus(Duration.ofSeconds(properties.resendCooldownSeconds())))) {
            throw UserManagementErrors.otpRateLimited();
        }

        if (challengeRepository.countRequestsSince(normalizedMobile, now.minus(Duration.ofHours(1)))
                >= properties.maxRequestsPerHour()) {
            throw UserManagementErrors.otpRateLimited();
        }

        // Any still-open challenge for this number is invalidated by the new one.
        challengeRepository.supersedeActive(normalizedMobile);

        String code = generateCode();
        OtpChallenge challenge = OtpChallenge.builder()
                .mobileNumber(normalizedMobile)
                .code(code)
                .status(OtpChallengeStatus.CREATED)
                .maxAttempts(properties.maxAttempts())
                .expiresAt(now.plus(Duration.ofSeconds(properties.ttlSeconds())))
                .build();
        challengeRepository.save(challenge);

        log.info("OTP requested for mobile={}", mask(normalizedMobile));
        smsSender.send(normalizedMobile, "Benji verification code: " + code);
    }

    /**
     * Verifies a submitted code. On success the challenge transitions to
     * VERIFIED, which both consumes it (no replay) and marks the mobile as proven.
     */
    @Transactional
    public void verifyOtp(String normalizedMobile, String candidateCode) {
        Instant now = clock.instant();
        OtpChallenge challenge = challengeRepository.findLatestActive(normalizedMobile)
                .orElseThrow(UserManagementErrors::otpInvalid);

        if (challenge.isExpired(now)) {
            challenge.setStatus(OtpChallengeStatus.EXPIRED);
            challengeRepository.save(challenge);
            throw UserManagementErrors.otpExpired();
        }
        if (!challenge.hasAttemptsLeft()) {
            challenge.setStatus(OtpChallengeStatus.BLOCKED);
            challengeRepository.save(challenge);
            throw UserManagementErrors.otpTooManyAttempts();
        }

        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!challenge.matches(candidateCode)) {
            if (!challenge.hasAttemptsLeft()) {
                challenge.setStatus(OtpChallengeStatus.BLOCKED);
                log.warn("OTP blocked after max attempts for mobile={}", mask(normalizedMobile));
            } else {
                log.warn("OTP mismatch for mobile={} attempt={}", mask(normalizedMobile), challenge.getAttempts());
            }
            challengeRepository.save(challenge);
            throw UserManagementErrors.otpInvalid();
        }

        challenge.setStatus(OtpChallengeStatus.VERIFIED);
        challenge.setVerifiedAt(now);
        challengeRepository.save(challenge);
        log.info("OTP verified for mobile={}", mask(normalizedMobile));
    }

    private String generateCode() {
        int bound = switch (properties.length()) {
            case 4 -> 10_000;
            case 8 -> 100_000_000;
            default -> OTP_BOUND;
        };
        return String.format("%0" + properties.length() + "d", random.nextInt(bound));
    }

    private String mask(String mobile) {
        if (mobile == null || mobile.length() < 6) {
            return "***";
        }
        return mobile.substring(0, Math.min(mobile.length(), 5)) + "****"
                + mobile.substring(mobile.length() - 2);
    }
}
