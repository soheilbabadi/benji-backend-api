package social.benji.benji_backend_api.audit;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
class LostFoundAuditAdminController {
    private final LostFoundAuditService auditService;

    LostFoundAuditAdminController(LostFoundAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/lost-found/{publicId}")
    public List<LostFoundAuditService.AuditRevisionDto> lostFoundHistory(
            @PathVariable UUID publicId) {
        return auditService.revisionsForPublicAdvertisement(publicId);
    }
}
