package social.benji.benji_backend_api.payment.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import social.benji.benji_backend_api.model.User;
import social.benji.benji_backend_api.payment.application.PaymentUseCase;
import social.benji.benji_backend_api.payment.application.dto.CreatePaymentRequest;
import social.benji.benji_backend_api.payment.application.dto.PaymentResponse;
import social.benji.benji_backend_api.repository.UserRepository;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentUseCase paymentUseCase;
    private final UserRepository userRepository;

    @PostMapping("/request")
    public ResponseEntity<PaymentResponse> requestPayment(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreatePaymentRequest request) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        UUID userId = UUID.fromString(user.getId());
        return ResponseEntity.ok(paymentUseCase.createPayment(userId, request));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @RequestParam("Authority") String authority,
            @RequestParam(value = "Status", required = false) String status) {
        paymentUseCase.verifyCallback(authority);
        return ResponseEntity.ok("Payment verification completed");
    }
}
