package social.benji.benji_backend_api.usermanagement.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Role-assignment payload. The value is validated against the domain enum in
 * the service (single source of truth); this pattern only rejects obviously
 * malformed input before it reaches the application layer.
 */
public record AssignRoleRequest(

        @NotBlank(message = "Role must not be blank.")
        @Pattern(regexp = "(?i)^(USER|EXPERT|ADMIN)$",
                message = "Role must be one of USER, EXPERT, ADMIN.")
        String role) {
}
