package social.benji.benji_backend_api.usermanagement.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload for requesting (or resending) an OTP on a mobile number. The number
 * is normalized/validated by the service layer (E.164), so this DTO only
 * guards against blank input.
 */
public record OtpRequest(

        @NotBlank(message = "Mobile number must not be blank.")
        String mobile) {
}
