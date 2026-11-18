package social.benji.benji_backend_api.usermanagement.service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import social.benji.benji_backend_api.storage.MinioProperties;
import social.benji.benji_backend_api.storage.ObjectStorage;
import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.domain.exception.UserManagementErrors;
import social.benji.benji_backend_api.usermanagement.repository.UserRepository;

/**
 * Avatar handling for user profiles. Binaries go to the shared MinIO bucket via
 * the vendor-neutral {@link ObjectStorage} port; PostgreSQL stores only the
 * backend-generated object key and detected MIME type. Clients receive
 * short-lived presigned GET URLs (the bucket stays private). Compensation: if
 * the DB write fails after upload the fresh object is deleted; if replacing an
 * avatar the old object removal failure is logged as an orphan for cleanup.
 */
@Slf4j
@Service
public class DefaultUserAvatarService implements UserAvatarService {

    /** Hard cap for uploaded avatars (5 MB), matching the platform-wide image limit. */
    static final long MAX_AVATAR_BYTES = 5L * 1024 * 1024;

    /** Presigned URL lifetime handed to clients. */
    private static final Duration URL_EXPIRY = Duration.ofMinutes(10);

    /** MIME → stored extension; validated by magic bytes, not just the declared content-type. */
    private static final Map<String, String> ALLOWED = Map.of(
            "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");

    /** Random placeholder picture seeded on every freshly registered account. */
    static final String DEFAULT_AVATAR_SOURCE = "https://picsum.photos/200";

    private final UserRepository userRepository;
    private final ObjectStorage storage;
    private final MinioProperties minio;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public DefaultUserAvatarService(UserRepository userRepository,
                                    ObjectStorage storage,
                                    MinioProperties minio) {
        this.userRepository = userRepository;
        this.storage = storage;
        this.minio = minio;
    }

    @Override
    @Transactional
    public AvatarDto upload(UUID userId, String contentType, byte[] bytes) {
        User user = findActiveUser(userId);
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_AVATAR_BYTES) {
            throw UserManagementErrors.avatarTooLarge();
        }
        String mime = detectMime(bytes);
        // Declared type must agree with the sniffed one — reject spoofed uploads.
        if (mime == null || !mime.equalsIgnoreCase(contentType == null ? "" : contentType.trim())) {
            throw UserManagementErrors.invalidAvatar();
        }
        String ext = ALLOWED.get(mime);
        UUID avatarId = UUID.randomUUID();
        // Backend-generated key — the client filename is never used.
        String key = objectKeyFor(user.getId(), avatarId, ext);
        try {
            storage.put(minio.bucketName(), key,
                    new ByteArrayInputStream(bytes), bytes.length, mime);
        } catch (RuntimeException e) {
            throw UserManagementErrors.avatarStorageUnavailable();
        }

        String previousKey = user.getAvatarObjectKey();
        try {
            user.setAvatarObjectKey(key);
            user.setAvatarContentType(mime);
            userRepository.saveAndFlush(user);
        } catch (RuntimeException e) {
            storage.delete(minio.bucketName(), key); // compensate uploaded-but-unpersisted object
            throw e;
        }
        removeQuietly(previousKey, key);
        return new AvatarDto(key, presignedUrl(key));
    }

    @Override
    @Transactional(readOnly = true)
    public AvatarDto get(UUID userId) {
        User user = findActiveUser(userId);
        return toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AvatarDto getByUsername(String username) {
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(username)
                .filter(User::isActive)
                .orElseThrow(UserManagementErrors::profileNotFound);
        return toDto(user);
    }

    @Override
    @Transactional
    public void delete(UUID userId) {
        User user = findActiveUser(userId);
        String key = user.getAvatarObjectKey();
        if (key == null) {
            return; // idempotent
        }
        user.setAvatarObjectKey(null);
        user.setAvatarContentType(null);
        userRepository.saveAndFlush(user);
        removeQuietly(key, null);
    }

    /**
     * Best-effort seeding of the default picsum avatar right after registration.
     * Runs in its own transaction ({@code REQUIRES_NEW}) so a slow or failing
     * external fetch can never roll back — or delay — the account creation
     * itself. If anything goes wrong the user simply has no avatar until they
     * upload one via {@link #upload}.
     */
    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void assignDefaultAvatar(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isActive() || user.getAvatarObjectKey() != null) {
            return; // already seeded or replaced by the user
        }
        byte[] bytes;
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(DEFAULT_AVATAR_SOURCE))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<byte[]> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() / 100 != 2) {
                log.warn("default avatar fetch returned HTTP {} for user {}", resp.statusCode(), userId);
                return;
            }
            bytes = resp.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("default avatar fetch interrupted for user {}", userId);
            return;
        } catch (Exception e) {
            log.warn("default avatar fetch failed for user {}: {}", userId, e.toString());
            return;
        }
        String mime = detectMime(bytes);
        if (mime == null || bytes.length == 0 || bytes.length > MAX_AVATAR_BYTES) {
            log.warn("default avatar source returned unsupported payload for user {}", userId);
            return;
        }
        String ext = ALLOWED.get(mime);
        String key = objectKeyFor(userId, UUID.randomUUID(), ext);
        try {
            storage.put(minio.bucketName(), key, new ByteArrayInputStream(bytes), bytes.length, mime);
        } catch (RuntimeException e) {
            log.warn("default avatar upload to storage failed for user {}: {}", userId, e.toString());
            return;
        }
        try {
            user.setAvatarObjectKey(key);
            user.setAvatarContentType(mime);
            userRepository.saveAndFlush(user);
        } catch (RuntimeException e) {
            storage.delete(minio.bucketName(), key); // compensate uploaded-but-unpersisted object
            log.warn("default avatar persistence failed for user {}: {}", userId, e.toString());
        }
    }

    private AvatarDto toDto(User user) {
        String key = user.getAvatarObjectKey();
        return new AvatarDto(key, key == null ? null : presignedUrl(key));
    }

    private String presignedUrl(String key) {
        return storage.presignedGetUrl(minio.bucketName(), key, URL_EXPIRY);
    }

    /** Deletes the superseded object; failures leave an orphan we log for the cleanup sweep. */
    private void removeQuietly(String key, String keepKey) {
        if (key == null || key.equals(keepKey)) {
            return;
        }
        try {
            storage.delete(minio.bucketName(), key);
        } catch (RuntimeException e) {
            log.warn("orphaned avatar object after failed delete: {}", key);
        }
    }

    private User findActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(UserManagementErrors::profileNotFound);
    }

    static String objectKeyFor(UUID userId, UUID avatarId, String ext) {
        return "profile-pictures/%s/av_%s.%s".formatted(userId, avatarId, ext);
    }

    private static String detectMime(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}
