package social.benji.benji_backend_api.usermanagement.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Payload for verifying an OTP code and completing sign-in / registration.
 */
public record OtpVerifyRequest(

        @NotBlank(message = "Mobile number must not be blank.")
        String mobile,

        @NotBlank(message = "Code must not be blank.")
        @Pattern(regexp = "^\\d{4,8}$", message = "Code must be 4-8 digits.")
        String code) {
}
