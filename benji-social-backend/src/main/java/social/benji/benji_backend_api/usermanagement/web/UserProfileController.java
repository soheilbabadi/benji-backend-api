package social.benji.benji_backend_api.usermanagement.web;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;
import social.benji.benji_backend_api.usermanagement.service.UserAvatarService;
import social.benji.benji_backend_api.usermanagement.service.UserProfileService;
import social.benji.benji_backend_api.usermanagement.web.dto.UpdateUserProfileRequest;
import social.benji.benji_backend_api.usermanagement.web.dto.UserProfileDto;

import java.io.IOException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * General user profile endpoints: name, username, email, city, country and
 * other public attributes. The authenticated user manages their own profile;
 * other users' profiles are readable by username. Avatar binaries are stored
 * in MinIO via the shared object-storage port.
 */
@RestController
@RequestMapping("/api/users/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserAvatarService userAvatarService;

    public UserProfileController(UserProfileService userProfileService,
                                 UserAvatarService userAvatarService) {
        this.userProfileService = userProfileService;
        this.userAvatarService = userAvatarService;
    }

    /** Current authenticated user's full profile. */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserProfileDto myProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return userProfileService.getMyProfile(principal.id());
    }

    /** Partial update of the authenticated user's profile (PATCH semantics). */
    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserProfileDto updateMyProfile(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody UpdateUserProfileRequest request) {
        UserProfileDto updated = userProfileService.updateProfile(principal.id(), request);
        // First profile completion doubles as account finalization: seed a random
        // picsum default avatar if the user hasn't set one yet (best-effort, never fails).
        maybeSeedDefaultAvatar(principal.id());
        return updated;
    }

    /**
     * Seeds the default https://picsum.photos/200 avatar on first profile
     * completion. Runs in its own transaction inside the service; failures are
     * logged and swallowed so profile updates never break.
     */
    private void maybeSeedDefaultAvatar(java.util.UUID userId) {
        try {
            userAvatarService.assignDefaultAvatar(userId);
        } catch (RuntimeException e) {
            // defensive: seeding must never surface to the client
        }
    }

    /** Uploads (replaces) the authenticated user's avatar picture to MinIO. */
    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public UserAvatarService.AvatarDto uploadMyAvatar(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) throws IOException {
        return userAvatarService.upload(principal.id(), file.getContentType(), file.getBytes());
    }

    /** Avatar state of the authenticated user, including a fresh presigned URL. */
    @GetMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public UserAvatarService.AvatarDto myAvatar(@AuthenticationPrincipal UserPrincipal principal) {
        return userAvatarService.get(principal.id());
    }

    /** Removes the authenticated user's avatar. Idempotent. */
    @DeleteMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public void deleteMyAvatar(@AuthenticationPrincipal UserPrincipal principal) {
        userAvatarService.delete(principal.id());
    }

    /** Public profile lookup by unique username handle. */
    @GetMapping("/{username}")
    public UserProfileDto byUsername(@PathVariable String username) {
        return userProfileService.getByUsername(username);
    }

    /** Public avatar lookup by username. */
    @GetMapping("/{username}/avatar")
    public UserAvatarService.AvatarDto avatarByUsername(@PathVariable String username) {
        return userAvatarService.getByUsername(username);
    }
}
