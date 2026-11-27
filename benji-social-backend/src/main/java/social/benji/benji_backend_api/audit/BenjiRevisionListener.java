package social.benji.benji_backend_api.audit;

import org.hibernate.envers.RevisionListener;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.model.LegacyUser;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

import java.util.UUID;

public class BenjiRevisionListener implements RevisionListener {
    @Override
    public void newRevision(Object revisionEntity) {
        BenjiRevisionEntity revision = (BenjiRevisionEntity) revisionEntity;
        revision.setActorType(BenjiActorType.SYSTEM);
        revision.setActorUserId(null);

        String correlationId = MDC.get("correlationId");
        if (correlationId != null && !correlationId.isBlank()) {
            revision.setCorrelationId(correlationId);
        }

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context == null ? null : context.getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return;
        }

        UUID actorId = null;
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            actorId = principal.id();
        } else if (authentication.getPrincipal() instanceof User user) {
            actorId = user.getId();
        } else if (authentication.getPrincipal() instanceof LegacyUser user) {
            try {
                actorId = UUID.fromString(user.getId());
            } catch (RuntimeException ignored) {
                actorId = null;
            }
        }

        if (actorId == null) {
            return;
        }
        revision.setActorUserId(actorId);
        boolean administrator = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        revision.setActorType(administrator ? BenjiActorType.ADMIN : BenjiActorType.USER);
    }
}
