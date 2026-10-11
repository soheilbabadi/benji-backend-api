package social.benji.benji_backend_api.usermanagement.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Signing key derived from the configured secret. HS256 needs >= 256 bits of
 * key material; a weak/missing secret fails fast at startup rather than
 * silently producing forgeable tokens.
 */
@Configuration
public class SecurityKeyConfig {

    @Bean
    public SecretKey jwtSigningKey(JwtProperties properties) {
        byte[] secret = properties.secret() == null
                ? new byte[0]
                : properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException(
                    "benji.security.jwt.secret must be at least 32 bytes (256 bits)");
        }
        return new SecretKeySpec(secret, "HmacSHA256");
    }
}
