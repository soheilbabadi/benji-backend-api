package social.benji.benji_backend_api.usermanagement.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.domain.UserRole;
import social.benji.benji_backend_api.usermanagement.domain.exception.UserManagementErrors;
import social.benji.benji_backend_api.usermanagement.repository.UserRepository;
import social.benji.benji_backend_api.usermanagement.web.dto.AssignRoleRequest;
import social.benji.benji_backend_api.usermanagement.web.dto.UserRoleDto;

/**
 * Default role-assignment service. Guards:
 * <ul>
 *   <li>target must be an active (non-deleted, non-blocked) user;</li>
 *   <li>an admin may not change their own role — this prevents the seeded
 *       bootstrap admin from locking itself out and makes privilege removal a
 *       deliberate two-admin operation;</li>
 *   <li>the requested role must exist in {@link UserRole}.</li>
 * </ul>
 * Every successful change is written to the application log with actor and
 * target ids for auditability.
 */
@Slf4j
@Service
public class DefaultUserRoleService implements UserRoleService {

    private final UserRepository userRepository;

    public DefaultUserRoleService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserRole> assignableRoles() {
        return List.of(UserRole.values());
    }

    @Override
    @Transactional(readOnly = true)
    public UserRoleDto getRole(UUID userId) {
        return toDto(findActiveUser(userId));
    }

    @Override
    @Transactional
    public UserRoleDto assignRole(UUID actorUserId, UUID targetUserId, AssignRoleRequest request) {
        if (request == null || request.role() == null || request.role().isBlank()) {
            throw UserManagementErrors.invalidRole(request == null ? null : request.role());
        }
        UserRole role;
        try {
            role = UserRole.valueOf(request.role().trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw UserManagementErrors.invalidRole(request.role());
        }
        return assignRole(actorUserId, targetUserId, role);
    }

    @Override
    @Transactional
    public UserRoleDto assignRole(UUID actorUserId, UUID targetUserId, UserRole role) {
        if (role == null) {
            throw UserManagementErrors.invalidRole(null);
        }
        if (actorUserId != null && actorUserId.equals(targetUserId)) {
            throw UserManagementErrors.cannotChangeOwnRole();
        }
        User target = findActiveUser(targetUserId);
        UserRole previous = target.getRole();
        if (previous == role) {
            // Idempotent: re-applying the same role is a no-op but still returns state.
            return toDto(target);
        }
        target.setRole(role);
        User saved = userRepository.save(target);
        log.info("role assigned: actor={} target={} {} -> {}",
                actorUserId, saved.getId(), previous, saved.getRole());
        return toDto(saved);
    }

    private User findActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(UserManagementErrors::userNotFound);
    }

    private static UserRoleDto toDto(User user) {
        String name = ((user.getFirstName() == null ? "" : user.getFirstName() + " ")
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return new UserRoleDto(
                user.getId(),
                name.isEmpty() ? null : name,
                user.getUsername(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getUpdatedAt());
    }
}
