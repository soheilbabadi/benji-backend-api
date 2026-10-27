package social.benji.benji_backend_api.usermanagement.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Server-side record of issued refresh tokens. The raw token is never stored;
 * only its SHA-256 hash, so a database leak cannot be replayed.
 */
@Entity
@Table(name = "tbl_refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Stable user reference; no JPA association to keep module boundaries loose. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    /** Set when this token was rotated into a successor (theft-detection lineage). */
    @Column(name = "replaced_by_token_hash", length = 64)
    private String replacedByTokenHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public boolean matches(String rawToken) {
        return MessageDigest.isEqual(
                tokenHash.getBytes(StandardCharsets.UTF_8),
                hash(rawToken).getBytes(StandardCharsets.UTF_8));
    }

    public boolean isValid(Instant now) {
        return revokedAt == null && now.isBefore(expiresAt);
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }
}
