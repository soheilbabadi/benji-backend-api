package social.benji.benji_backend_api.payment.infrastructure.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;
import social.benji.benji_backend_api.payment.application.PaymentGateway;
import social.benji.benji_backend_api.payment.config.ZarinPalProperties;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@EnableConfigurationProperties(ZarinPalProperties.class)
public class ZarinPalPaymentGateway implements PaymentGateway {

    private final ZarinPalProperties properties;
    private final RestClient restClient;

    public ZarinPalPaymentGateway(ZarinPalProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getApiUrl())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public GatewayRequestResult requestPayment(UUID transactionId, long amountRials) {
        if (properties.getMerchantId() == null || properties.getMerchantId().isBlank()) {
            throw new IllegalStateException("ZarinPal merchant ID is not configured");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("merchant_id", properties.getMerchantId());
        body.put("amount", amountRials);
        body.put("callback_url", properties.getCallbackUrl());
        body.put("description", "Benji payment " + transactionId);

        try {
            JsonNode response = restClient.post()
                    .uri("/payment/request.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode data = response == null ? null : response.path("data");
            int code = data == null ? -1 : data.path("code").asInt(-1);
            String authority = data == null ? null : data.path("authority").asText(null);
            if (code != 100 && code != 101 || authority == null || authority.isBlank()) {
                throw new IllegalStateException("ZarinPal payment request was rejected");
            }
            String url = properties.getPaymentUrl() + authority;
            return new GatewayRequestResult(authority, url);
        } catch (RestClientException e) {
            throw new IllegalStateException("ZarinPal payment request failed", e);
        }
    }

    @Override
    public GatewayVerificationResult verifyPayment(String authority, long amountRials) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("merchant_id", properties.getMerchantId());
        body.put("amount", amountRials);
        body.put("authority", authority);

        try {
            JsonNode response = restClient.post()
                    .uri("/payment/verify.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode data = response == null ? null : response.path("data");
            int code = data == null ? -1 : data.path("code").asInt(-1);
            if (code == 100 || code == 101) {
                return new GatewayVerificationResult(true,
                        data.path("ref_id").asText(), null);
            }
            return new GatewayVerificationResult(false, null,
                    "ZarinPal verification rejected code " + code);
        } catch (RestClientException e) {
            throw new IllegalStateException("ZarinPal verification request timed out or failed", e);
        }
    }
}
