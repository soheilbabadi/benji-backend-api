package social.benji.benji_backend_api.usermanagement.service;

import java.util.UUID;

/**
 * Owns the user's avatar binary: bytes live in MinIO through the shared
 * {@code ObjectStorage} port, only the object key is persisted on the user row.
 */
public interface UserAvatarService {

    /** Replaces the current avatar (old object deleted best-effort). Returns the new state. */
    AvatarDto upload(UUID userId, String contentType, byte[] bytes);

    /** Short-lived presigned GET URL for private consumption; {@code null} when no avatar. */
    AvatarDto get(UUID userId);

    /** Publicly visible projection addressed by username; URL omitted when the profile is inactive. */
    AvatarDto getByUsername(String username);

    /** Removes the avatar object and its reference. Idempotent. */
    void delete(UUID userId);

    /**
     * Seeds a brand-new account with a random default picture fetched from
     * {@code https://picsum.photos/200} and stored in MinIO. Best-effort: any
     * failure (network, storage) is swallowed so registration never breaks;
     * the user can still upload their own photo afterwards via
     * {@link #upload}. Does nothing when an avatar is already set.
     */
    void assignDefaultAvatar(UUID userId);

    /**
     * @param objectKey storage key of the avatar; {@code null} when unset.
     * @param url       presigned GET URL valid for a few minutes; {@code null} when unset.
     */
    record AvatarDto(String objectKey, String url) {
    }
}
