package social.benji.benji_backend_api.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.zarinpal")
public class ZarinPalProperties {
    private String merchantId = "";
    private boolean sandbox = true;
    private String callbackUrl = "http://localhost:8080/api/payments/callback";
    private String apiUrl = "https://sandbox.zarinpal.com/pg/v4";
    private String paymentUrl = "https://sandbox.zarinpal.com/pg/StartPay/";
}
