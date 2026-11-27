package social.benji.benji_backend_api.consultation.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import social.benji.benji_backend_api.consultation.domain.model.Consultation;
import social.benji.benji_backend_api.consultation.repository.ConsultationRepository;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

import java.util.UUID;

@Component
public class ConsultationAuthorizationService {
    private final ConsultationRepository consultationRepository;

    public ConsultationAuthorizationService(ConsultationRepository consultationRepository) {
        this.consultationRepository = consultationRepository;
    }

    public UserPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)
                || principal.id() == null) {
            throw new BenjiCustomException.ResourceUnauthorizedException("Authentication is required");
        }
        return principal;
    }

    public String callerId() {
        return currentPrincipal().id().toString();
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new BenjiCustomException.ResourceForbiddenException("Administrator role is required");
        }
    }

    public Consultation requireConsultation(String consultationId) {
        return consultationRepository.findById(consultationId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException(
                        "Consultation not found"));
    }

    public Consultation requireParticipant(String consultationId) {
        Consultation consultation = requireConsultation(consultationId);
        if (isAdmin()) {
            return consultation;
        }
        String callerId = callerId();
        if (callerId.equals(consultation.getOwnerId())
                || callerId.equals(consultation.getAssignedExpertId())) {
            return consultation;
        }
        throw new BenjiCustomException.ResourceForbiddenException(
                "You are not authorized to access this consultation");
    }

    public Consultation requireOwner(String consultationId) {
        Consultation consultation = requireConsultation(consultationId);
        if (!isAdmin() && !callerId().equals(consultation.getOwnerId())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the consultation owner or an administrator can perform this operation");
        }
        return consultation;
    }

    public Consultation requireAssignedExpert(String consultationId) {
        Consultation consultation = requireConsultation(consultationId);
        if (isAdmin()) {
            return consultation;
        }
        if (!callerId().equals(consultation.getAssignedExpertId())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the assigned expert or an administrator can perform this operation");
        }
        return consultation;
    }

    public void requireOwnerOrAdminForUserId(String targetUserId) {
        if (!isAdmin() && !callerId().equals(targetUserId)) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the user or an administrator can perform this operation");
        }
    }

    public boolean isParticipant(Consultation consultation) {
        if (isAdmin()) {
            return true;
        }
        String callerId = callerId();
        return callerId.equals(consultation.getOwnerId())
                || callerId.equals(consultation.getAssignedExpertId());
    }
}
