package social.benji.benji_backend_api.usermanagement.service;

import java.util.UUID;

import java.time.Duration;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import social.benji.benji_backend_api.storage.MinioProperties;
import social.benji.benji_backend_api.storage.ObjectStorage;
import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.domain.exception.UserManagementErrors;
import social.benji.benji_backend_api.usermanagement.repository.UserRepository;
import social.benji.benji_backend_api.usermanagement.web.dto.UpdateUserProfileRequest;
import social.benji.benji_backend_api.usermanagement.web.dto.UserProfileDto;

@Service
public class DefaultUserProfileService implements UserProfileService {

    /** Avatar URL lifetime embedded in profile responses. */
    private static final Duration AVATAR_URL_EXPIRY = Duration.ofMinutes(10);

    private final UserRepository userRepository;
    private final ObjectStorage storage;
    private final MinioProperties minio;

    public DefaultUserProfileService(UserRepository userRepository,
                                     ObjectStorage storage,
                                     MinioProperties minio) {
        this.userRepository = userRepository;
        this.storage = storage;
        this.minio = minio;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getMyProfile(UUID userId) {
        return toDto(findActiveUser(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getByUsername(String username) {
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedAtIsNull(username)
                .filter(User::isActive)
                .orElseThrow(UserManagementErrors::profileNotFound);
        return toDto(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(UUID userId, UpdateUserProfileRequest request) {
        User user = findActiveUser(userId);

        // name: "Ali Reza" -> firstName="Ali Reza", lastName kept unless the
        // supplied name clearly carries a second part.
        if (request.name() != null) {
            String trimmed = request.name().trim();
            if (trimmed.isEmpty()) {
                throw UserManagementErrors.nameRequired();
            }
            applyName(user, trimmed);
        }
        if (request.username() != null) {
            String candidate = normalizeUsername(request.username());
            if (!candidate.equalsIgnoreCase(user.getUsername())
                    && userRepository.usernameTakenByAnother(candidate, user.getId())) {
                throw UserManagementErrors.usernameAlreadyInUse();
            }
            user.setUsername(candidate);
        }
        if (request.email() != null && !request.email().isBlank()) {
            String normalizedEmail = request.email().trim().toLowerCase();
            if (!normalizedEmail.equals(user.getEmail())
                    && userRepository.existsByEmailAndDeletedAtIsNull(normalizedEmail)) {
                throw UserManagementErrors.emailAlreadyInUse();
            }
            user.setEmail(normalizedEmail);
            // Ownership of an address is only proven through a future verification flow.
            user.setEmailVerified(false);
        }
        if (request.city() != null) {
            user.setCity(blankToNull(request.city()));
        }
        if (request.country() != null) {
            user.setCountry(blankToNull(request.country()));
        }
        if (request.bio() != null) {
            user.setBio(blankToNull(request.bio()));
        }
        if (request.phone() != null) {
            user.setPhone(blankToNull(request.phone()));
        }
        if (request.website() != null) {
            user.setWebsite(blankToNull(request.website()));
        }
        if (request.birthDate() != null) {
            user.setBirthDate(blankToNull(request.birthDate()));
        }
        if (request.gender() != null) {
            user.setGender(blankToNull(request.gender()));
        }

        try {
            return toDto(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            // Concurrent claim of the same handle settles on the DB unique index.
            throw UserManagementErrors.usernameAlreadyInUse();
        }
    }

    private User findActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(UserManagementErrors::profileNotFound);
    }

    private void applyName(User user, String fullName) {
        int space = fullName.lastIndexOf(' ');
        if (space > 0) {
            user.setFirstName(fullName.substring(0, space).trim());
            user.setLastName(fullName.substring(space + 1).trim());
        } else {
            user.setFirstName(fullName);
        }
    }

    private String normalizeUsername(String raw) {
        String value = raw.trim().toLowerCase();
        if (value.length() < 3 || value.length() > 30 || !value.matches("^[a-z0-9._]+$")) {
            throw UserManagementErrors.invalidUsername();
        }
        return value;
    }

    private static String blankToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private UserProfileDto toDto(User user) {
        String name = ((user.getFirstName() == null ? "" : user.getFirstName() + " ")
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return new UserProfileDto(
                user.getId(),
                name.isEmpty() ? null : name,
                user.getUsername(),
                user.getEmail(),
                user.getCity(),
                user.getCountry(),
                user.getBio(),
                user.getPhone(),
                user.getWebsite(),
                user.getBirthDate(),
                user.getGender(),
                avatarUrl(user),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    /** Short-lived presigned GET URL for the stored avatar; {@code null} when unset. */
    private String avatarUrl(User user) {
        String key = user.getAvatarObjectKey();
        if (key == null) {
            return null;
        }
        try {
            return storage.presignedGetUrl(minio.bucketName(), key, AVATAR_URL_EXPIRY);
        } catch (RuntimeException e) {
            // A profile read must not fail because the storage is briefly unavailable.
            return null;
        }
    }
}
