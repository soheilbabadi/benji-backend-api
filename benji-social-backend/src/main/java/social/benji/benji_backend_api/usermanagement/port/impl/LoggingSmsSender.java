package social.benji.benji_backend_api.usermanagement.port.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import social.benji.benji_backend_api.usermanagement.port.SmsSender;

/**
 * Development-only sender. Logs the destination but NEVER the OTP body, even in
 * dev, so secrets cannot leak into aggregated logs by accident. Real providers
 * register their own {@code SmsSender} bean under a non-dev profile.
 */
@Component
@Profile("dev")
public class LoggingSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    @Override
    public void send(String to, String message) {
        log.info("SMS requested to {} (body suppressed)", mask(to));
    }

    private String mask(String mobile) {
        if (mobile == null || mobile.length() < 6) {
            return "***";
        }
        return mobile.substring(0, Math.min(mobile.length(), 5)) + "****"
                + mobile.substring(mobile.length() - 2);
    }
}
