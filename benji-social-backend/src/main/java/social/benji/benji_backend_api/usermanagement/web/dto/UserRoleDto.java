package social.benji.benji_backend_api.usermanagement.web.dto;

import java.time.Instant;
import java.util.UUID;

/** Administrative view of a user's role state. No private profile data here. */
public record UserRoleDto(
        UUID userId,
        String name,
        String username,
        String role,
        String status,
        Instant updatedAt) {
}
