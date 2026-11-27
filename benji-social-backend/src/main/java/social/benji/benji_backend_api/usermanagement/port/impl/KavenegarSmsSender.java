package social.benji.benji_backend_api.usermanagement.port.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import social.benji.benji_backend_api.usermanagement.port.SmsSender;

/**
 * Production {@link SmsSender} backed by the Kavenegar SMS API
 * (https://api.kavenegar.com). Sends plain messages through
 * {@code POST /v1/{token}/sms/send.json}.
 *
 * <p>Registered only under non-dev profiles so {@link LoggingSmsSender} stays
 * active in local development. OTP bodies are treated as secret material:
 * they are never logged, and the token is stripped from every error message.
 */
@Component
@Profile("!dev")
public class KavenegarSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(KavenegarSmsSender.class);

    private final KavenegarSmsProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KavenegarSmsSender(KavenegarSmsProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
    }

    @Override
    public void send(String to, String message) {
        if (properties.getApiToken() == null || properties.getApiToken().isBlank()) {
            throw new IllegalStateException(
                    "Kavenegar API token not configured (benji.sms.kavenegar.api-token)");
        }

        String form = encodeParams(to, message);
        URI uri = URI.create(properties.getBaseUrl() + "/" + properties.getApiToken()
                + "/sms/send.json");

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(properties.getReadTimeout())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponse(response, to);
        } catch (IOException ex) {
            // Message may contain an OTP — do not log it, only the masked recipient.
            throw new IllegalStateException(
                    "Kavenegar send failed for " + mask(to) + ": " + redact(ex.getMessage()), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kavenegar send interrupted for " + mask(to), ex);
        }
    }

    private void handleResponse(HttpResponse<String> response, String to) {
        int status = response.statusCode();
        JsonNode body = parseQuietly(response.body());

        // Kavenegar reports errors with HTTP 200 and a non-zero errorCode.
        Integer errorCode = body != null && body.hasNonNull("errorCode")
                ? body.get("errorCode").asInt() : null;

        if (status >= 200 && status < 300 && (errorCode == null || errorCode == 0)) {
            log.debug("SMS accepted by Kavenegar for {}", mask(to));
            return;
        }

        String errorMessage = body != null && body.hasNonNull("errorMessage")
                ? body.get("errorMessage").asText() : redact(response.body());
        throw new IllegalStateException("Kavenegar rejected SMS for " + mask(to)
                + " (http=" + status + ", code=" + errorCode + "): " + errorMessage);
    }

    private JsonNode parseQuietly(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String encodeParams(String to, String message) {
        StringJoiner joiner = new StringJoiner("&");
        joiner.add("receiver=" + URLEncoder.encode(to, StandardCharsets.UTF_8));
        joiner.add("message=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
        if (properties.getSender() != null && !properties.getSender().isBlank()) {
            joiner.add("sender=" + URLEncoder.encode(properties.getSender(), StandardCharsets.UTF_8));
        }
        return joiner.toString();
    }

    /** Removes the API token from any text before it can reach logs or exceptions. */
    private String redact(String text) {
        if (text == null) {
            return "";
        }
        return text.replace(properties.getApiToken(), "[REDACTED]");
    }

    private String mask(String mobile) {
        if (mobile == null || mobile.length() < 6) {
            return "***";
        }
        return mobile.substring(0, Math.min(mobile.length(), 5)) + "****"
                + mobile.substring(mobile.length() - 2);
    }
}
