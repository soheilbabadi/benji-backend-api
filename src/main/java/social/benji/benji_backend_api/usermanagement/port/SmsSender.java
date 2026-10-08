package social.benji.benji_backend_api.usermanagement.port;

/**
 * Outbound port for SMS delivery. The module never depends on a concrete
 * provider; implementations (Kavenegar, Twilio, logging stub) plug in here.
 */
public interface SmsSender {

    /**
     * @param to        normalized E.164 mobile number
     * @param message   body text containing the OTP — treat as secret material
     */
    void send(String to, String message);
}
