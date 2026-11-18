package social.benji.benji_backend_api.usermanagement.web.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Public projection of a user's general profile information.
 * Sensitive identity fields (mobile number) are never exposed here.
 * The avatar is delivered as a short-lived presigned MinIO URL, never as raw bytes.
 */
public record UserProfileDto(
        UUID userId,
        String name,
        String username,
        String email,
        String city,
        String country,
        String bio,
        String phone,
        String website,
        String birthDate,
        String gender,
        /** Presigned GET URL for the avatar; {@code null} when no picture was uploaded. */
        String avatarUrl,
        Instant createdAt,
        Instant updatedAt) {
}
