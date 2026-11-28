package social.benji.benji_backend_api.consultation.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import social.benji.benji_backend_api.consultation.domain.model.Consultation;
import social.benji.benji_backend_api.consultation.repository.ConsultationRepository;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserPrincipal
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
        if (isAdmin()) {
            return;
        }
        try {
            if (callerId().equals(UUID.fromString(targetUserId).toString())) {
                return;
            }
        } catch (IllegalArgumentException ignored) {
            // Malformed user identifiers cannot match the canonical authenticated ID.
        }
        throw new BenjiCustomException.ResourceForbiddenException(
                "Only the user or an administrator can perform this operation");
    }

    public List<Consultation> accessibleConsultations() {
        if (isAdmin()) {
            return consultationRepository.findAll();
        }
        String callerId = callerId();
        return Stream.concat(
                        consultationRepository.findByOwnerId(callerId).stream(),
                        consultationRepository.findByAssignedExpertId(callerId).stream())
                .collect(java.util.stream.Collectors.toMap(
                        Consultation::getId,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new))
                .values()
                .stream()
                .toList();
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
