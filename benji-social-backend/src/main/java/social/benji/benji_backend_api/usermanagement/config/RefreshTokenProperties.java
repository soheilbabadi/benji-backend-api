package social.benji.benji_backend_api.usermanagement.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "benji.security.refresh-token")
public record RefreshTokenProperties(Duration ttl, boolean rotationEnabled) {

    public RefreshTokenProperties {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            ttl = Duration.ofDays(30);
        }
        if (!rotationEnabled) {
            // Rotation is a security default, not an opt-in.
            rotationEnabled = true;
        }
    }
}
