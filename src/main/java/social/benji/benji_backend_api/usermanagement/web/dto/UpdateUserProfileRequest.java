package social.benji.benji_backend_api.usermanagement.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Partial-update request for the authenticated user's profile. Only supplied
 * (non-null) fields are applied; blank values clear optional fields.
 */
public record UpdateUserProfileRequest(

        @Size(max = 100, message = "Name must be at most 100 characters.")
        String name,

        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters.")
        @Pattern(regexp = "^[a-zA-Z0-9._]+$",
                message = "Username may only contain letters, digits, dots and underscores.")
        String username,

        @Email(message = "Email address is not valid.")
        @Size(max = 254, message = "Email must be at most 254 characters.")
        String email,

        @Size(max = 100, message = "City must be at most 100 characters.")
        String city,

        @Size(max = 100, message = "Country must be at most 100 characters.")
        String country,

        @Size(max = 500, message = "Bio must be at most 500 characters.")
        String bio,

        @Size(max = 20, message = "Phone must be at most 20 characters.")
        String phone,

        @Size(max = 200, message = "Website must be at most 200 characters.")
        String website,

        @Size(max = 10, message = "Birth date must be in ISO format (yyyy-MM-dd).")
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$",
                message = "Birth date must be in ISO format (yyyy-MM-dd).")
        String birthDate,

        @Size(max = 20, message = "Gender must be at most 20 characters.")
        String gender) {
}
