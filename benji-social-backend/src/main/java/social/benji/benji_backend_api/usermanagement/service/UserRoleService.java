package social.benji.benji_backend_api.usermanagement.service;

import java.util.List;
import java.util.UUID;

import social.benji.benji_backend_api.usermanagement.domain.UserRole;
import social.benji.benji_backend_api.usermanagement.web.dto.AssignRoleRequest;
import social.benji.benji_backend_api.usermanagement.web.dto.UserRoleDto;

/**
 * Administrative role management. Only holders of {@code ROLE_ADMIN} may call
 * the web layer that delegates here (SecurityConfig plus {@code @PreAuthorize}
 * on the controller). The single-role MVP model keeps exactly one role per
 * user in {@code tbl_users.role}; JWT principals are rebuilt from the database
 * on every request, so a change takes effect immediately for the target user.
 */
public interface UserRoleService {

    /** Current list of assignable roles — derived from the domain enum, never hardcoded. */
    List<UserRole> assignableRoles();

    /** Role state of one user. Uniform 404-style error for unknown/deleted ids. */
    UserRoleDto getRole(UUID userId);

    /** Assigns (or replaces) the role of the target user. Admin-only at the API layer. */
    UserRoleDto assignRole(UUID actorUserId, UUID targetUserId, AssignRoleRequest request);

    /** Convenience overload used by callers that already hold a parsed enum. */
    UserRoleDto assignRole(UUID actorUserId, UUID targetUserId, UserRole role);
}
