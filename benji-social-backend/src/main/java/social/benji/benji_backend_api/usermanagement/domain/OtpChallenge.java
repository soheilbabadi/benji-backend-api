package social.benji.benji_backend_api.usermanagement.domain;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One OTP challenge per request. Lives outside the User aggregate so
 * authentication protocol state never pollutes identity data.
 */
@Entity
@Table(name = "tbl_otp_challenges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpChallenge {

    private static final int DEFAULT_MAX_ATTEMPTS = 5;

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    /**
     * NOTE: stored in plain text per explicit product decision for the MVP.
     * Must never appear in logs or API responses.
     */
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OtpChallengeStatus status = OtpChallengeStatus.CREATED;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private int maxAttempts = DEFAULT_MAX_ATTEMPTS;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean hasAttemptsLeft() {
        return attempts < maxAttempts;
    }

    /** Constant-time comparison to avoid timing side channels on code guessing. */
    public boolean matches(String candidate) {
        return MessageDigest.isEqual(
                code.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                candidate.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public boolean isUsable(Instant now) {
        return status == OtpChallengeStatus.CREATED && !isExpired(now) && hasAttemptsLeft();
    }
}
