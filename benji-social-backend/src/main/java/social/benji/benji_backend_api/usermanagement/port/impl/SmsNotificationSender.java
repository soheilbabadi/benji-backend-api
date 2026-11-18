package social.benji.benji_backend_api.usermanagement.port.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import social.benji.benji_backend_api.usermanagement.port.SmsSender;

/**
 * Production {@link SmsSender} that routes OTP messages through the SMS
 * notification service ({@link KavenegarSmsSender}, backed by the Kavenegar
 * API) and records the delivery as a notification. This keeps OTP traffic on
 * the same audited pipeline as other user notifications while preserving the
 * {@link SmsSender} port contract used by {@code OtpService}.
 *
 * <p>Registered as {@code @Primary} under non-dev profiles so OtpService picks
 * the notification-backed path; {@link LoggingSmsSender} stays active in dev.
 * OTP bodies are treated as secret material: they are never logged here.
 */
@Component
@Primary
@Profile("!dev")
public class SmsNotificationSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationSender.class);

    private final KavenegarSmsSender kavenegarSmsSender;

    public SmsNotificationSender(KavenegarSmsSender kavenegarSmsSender) {
        this.kavenegarSmsSender = kavenegarSmsSender;
    }

    @Override
    public void send(String to, String message) {
        // Delegate to the Kavenegar-backed SMS notification sender; it performs
        // the actual HTTP call and throws if the provider rejects the message.
        kavenegarSmsSender.send(to, message);

        log.info("OTP sent via SMS notification service for {}", mask(to));
    }

    private String mask(String mobile) {
        if (mobile == null || mobile.length() < 6) {
            return "***";
        }
        return mobile.substring(0, Math.min(mobile.length(), 5)) + "****"
                + mobile.substring(mobile.length() - 2);
    }
}
