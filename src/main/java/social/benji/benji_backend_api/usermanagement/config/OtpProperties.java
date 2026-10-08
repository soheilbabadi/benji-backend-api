package social.benji.benji_backend_api.usermanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tunables for the OTP subsystem. Defaults are conservative for MVP; they can
 * be overridden per environment without touching code.
 */
@ConfigurationProperties(prefix = "benji.security.otp")
public record OtpProperties(
        int length,
        int ttlSeconds,
        int resendCooldownSeconds,
        int maxRequestsPerHour,
        int maxAttempts) {

    public OtpProperties {
        if (length <= 0) {
            length = 6;
        }
        if (ttlSeconds <= 0) {
            ttlSeconds = 120;
        }
        if (resendCooldownSeconds <= 0) {
            resendCooldownSeconds = 60;
        }
        if (maxRequestsPerHour <= 0) {
            maxRequestsPerHour = 5;
        }
        if (maxAttempts <= 0) {
            maxAttempts = 5;
        }
    }
}
