package social.benji.benji_backend_api.usermanagement.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "benji.security.jwt")
public record JwtProperties(String secret, String algorithm, Duration accessTokenTtl) {

    public JwtProperties {
        if (algorithm == null || algorithm.isBlank()) {
            algorithm = "HS256";
        }
        if (accessTokenTtl == null || accessTokenTtl.isZero() || accessTokenTtl.isNegative()) {
            accessTokenTtl = Duration.ofMinutes(15);
        }
    }
}
