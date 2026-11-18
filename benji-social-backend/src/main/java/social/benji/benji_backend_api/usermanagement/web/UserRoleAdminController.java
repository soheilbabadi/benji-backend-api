package social.benji.benji_backend_api.usermanagement.web;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import social.benji.benji_backend_api.usermanagement.domain.UserRole;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;
import social.benji.benji_backend_api.usermanagement.service.UserRoleService;
import social.benji.benji_backend_api.usermanagement.web.dto.AssignRoleRequest;
import social.benji.benji_backend_api.usermanagement.web.dto.UserRoleDto;

/**
 * Admin-only role management. SecurityConfig already gates every
 * {@code /api/admin/**} request on ROLE_ADMIN (principals are rebuilt from the
 * database per request, so grants/revokes apply immediately); the class-level
 * {@code @PreAuthorize} mirrors that at the method layer as defense in depth.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserRoleAdminController {

    private final UserRoleService userRoleService;

    public UserRoleAdminController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    /** Roles that can be assigned (derived from the domain enum). */
    @GetMapping("/roles")
    public List<UserRole> assignableRoles() {
        return userRoleService.assignableRoles();
    }

    /** Current role state of one user. */
    @GetMapping("/{userId}/role")
    public UserRoleDto getRole(@PathVariable UUID userId) {
        return userRoleService.getRole(userId);
    }

    /** Assigns (replaces) a user's role, e.g. {"role": "EXPERT"}. */
    @PutMapping("/{userId}/role")
    public UserRoleDto assignRole(@AuthenticationPrincipal UserPrincipal admin,
                                  @PathVariable UUID userId,
                                  @Valid @RequestBody AssignRoleRequest request) {
        return userRoleService.assignRole(admin.id(), userId, request);
    }
}
