package social.benji.benji_backend_api.payment.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social.benji.benji_backend_api.payment.application.PaymentUseCase;
import social.benji.benji_backend_api.payment.application.dto.CreatePaymentRequest;
import social.benji.benji_backend_api.payment.application.dto.PaymentResponse;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentUseCase paymentUseCase;

    @PostMapping("/request")
    public ResponseEntity<PaymentResponse> requestPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentUseCase.createPayment(principal.id(), request));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam("Authority") String authority,
            @RequestParam(value = "Status", required = false) String status) {
        paymentUseCase.verifyCallback(authority);
        return ResponseEntity.ok("Payment verification completed");
    }
}
