package social.benji.benji_backend_api.usermanagement.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import social.benji.benji_backend_api.usermanagement.config.RefreshTokenProperties;
import social.benji.benji_backend_api.usermanagement.domain.RefreshToken;
import social.benji.benji_backend_api.usermanagement.domain.exception.UserManagementErrors;
import social.benji.benji_backend_api.usermanagement.repository.RefreshTokenRepository;

/**
 * Issues and rotates opaque refresh tokens. Only SHA-256 hashes are persisted;
 * presenting an already-rotated token is treated as theft and revokes the
 * whole family for that user.
 */
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final int RAW_TOKEN_BYTES = 32;

    private final RefreshTokenRepository repository;
    private final RefreshTokenProperties properties;
    private final SecureRandom random;
    private final Clock clock;

    public TokenService(RefreshTokenRepository repository,
                        RefreshTokenProperties properties,
                        SecureRandom secureRandom,
                        Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.random = secureRandom;
        this.clock = clock;
    }

    /** @return raw token to hand to the client (persisted only as a hash). */
    @Transactional
    public String issue(UUID userId) {
        Instant now = clock.instant();
        String raw = generateRawToken();
        repository.save(RefreshToken.builder()
                .userId(userId)
                .tokenHash(RefreshToken.hash(raw))
                .expiresAt(now.plus(properties.ttl()))
                .build());
        return raw;
    }

    /**
     * Validates and consumes a refresh token, returning its owner's id and the
     * replacement raw token when rotation is enabled.
     */
    @Transactional
    public RefreshResult refresh(String rawToken) {
        Instant now = clock.instant();
        RefreshToken stored = repository.findByTokenHash(RefreshToken.hash(rawToken))
                .orElseThrow(UserManagementErrors::invalidRefreshToken);

        if (stored.getRevokedAt() != null) {
            // Reuse of a rotated/revoked token => assume theft, kill all live sessions.
            log.warn("Refresh token reuse detected for user={}, revoking all sessions", stored.getUserId());
            repository.revokeAllForUser(stored.getUserId(), now);
            throw UserManagementErrors.invalidRefreshToken();
        }
        if (!stored.isValid(now)) {
            throw UserManagementErrors.invalidRefreshToken();
        }

        UUID userId = stored.getUserId();
        if (properties.rotationEnabled()) {
            String replacement = generateRawToken();
            Instant expiry = now.plus(properties.ttl());
            RefreshToken next = RefreshToken.builder()
                    .userId(userId)
                    .tokenHash(RefreshToken.hash(replacement))
                    .expiresAt(expiry)
                    .build();
            repository.save(next);
            stored.setRevokedAt(now);
            stored.setReplacedByTokenHash(next.getTokenHash());
            repository.save(stored);
            return new RefreshResult(userId, replacement);
        }
        return new RefreshResult(userId, null);
    }

    @Transactional
    public void revoke(String rawToken) {
        Instant now = clock.instant();
        repository.findByTokenHash(RefreshToken.hash(rawToken))
                .filter(t -> t.getRevokedAt() == null)
                .ifPresent(t -> {
                    t.setRevokedAt(now);
                    repository.save(t);
                });
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        repository.revokeAllForUser(userId, clock.instant());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[RAW_TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record RefreshResult(UUID userId, String newRawToken) {
    }
}
