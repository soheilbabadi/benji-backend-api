package social.benji.benji_backend_api.usermanagement.port.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code benji.sms.kavenegar.*} properties. The API token is never stored
 * in source control — supply it via the {@code KAVENEGAR_API_TOKEN} env var.
 */
@ConfigurationProperties(prefix = "benji.sms.kavenegar")
public class KavenegarSmsProperties {

    /** Base URL of the Kavenegar REST API. */
    private String baseUrl = "https://api.kavenegar.com/v1";

    /** Account API key (token) from the Kavenegar dashboard. */
    private String apiToken;

    /** Default sender line (3xxx number or a pattern number). */
    private String sender;

    /** HTTP connect timeout, e.g. "5s". */
    private java.time.Duration connectTimeout = java.time.Duration.ofSeconds(5);

    /** HTTP read timeout, e.g. "10s". */
    private java.time.Duration readTimeout = java.time.Duration.ofSeconds(10);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public java.time.Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(java.time.Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public java.time.Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(java.time.Duration readTimeout) {
        this.readTimeout = readTimeout;
    }
}
