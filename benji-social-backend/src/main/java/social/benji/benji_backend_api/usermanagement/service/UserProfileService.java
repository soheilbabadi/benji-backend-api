package social.benji.benji_backend_api.usermanagement.service;

import java.util.UUID;

import social.benji.benji_backend_api.usermanagement.web.dto.UpdateUserProfileRequest;
import social.benji.benji_backend_api.usermanagement.web.dto.UserProfileDto;

/**
 * Application service for general user profile information (name, username,
 * email, city, country and other public attributes). Registration itself stays
 * in {@link UserService}; this service owns reading and updating the profile.
 */
public interface UserProfileService {

    /** Full profile of the authenticated user (includes private fields such as email). */
    UserProfileDto getMyProfile(UUID userId);

    /** Public projection addressed by the unique username handle. */
    UserProfileDto getByUsername(String username);

    /** Applies a partial update to the authenticated user's profile. */
    UserProfileDto updateProfile(UUID userId, UpdateUserProfileRequest request);
}
