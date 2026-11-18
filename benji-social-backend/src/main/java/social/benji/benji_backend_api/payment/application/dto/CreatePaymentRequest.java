package social.benji.benji_backend_api.payment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull PaidServiceType serviceType,
        @NotNull UUID serviceReferenceId,
        @NotBlank String idempotencyKey
) {}
