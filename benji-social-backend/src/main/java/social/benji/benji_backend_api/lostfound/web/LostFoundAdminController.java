package social.benji.benji_backend_api.lostfound.web;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social.benji.benji_backend_api.lostfound.domain.ReportStatus;
import social.benji.benji_backend_api.lostfound.repository.AdvertisementReportRepository;
import social.benji.benji_backend_api.lostfound.service.LostFoundAdvertisementService;
import social.benji.benji_backend_api.lostfound.web.dto.LostFoundDtos.*;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

import java.util.UUID;

/** Minimal MVP moderation console for admins (ROLE_ADMIN enforced by SecurityConfig). */
@RestController
@RequestMapping("/api/admin/lost-found")
@PreAuthorize("hasRole('ADMIN')")
class LostFoundAdminController {

    private final LostFoundAdvertisementService service;
    private final AdvertisementReportRepository reports;
    private final LostFoundMapper mapper;

    LostFoundAdminController(LostFoundAdvertisementService service,
                             AdvertisementReportRepository reports,
                             LostFoundMapper mapper) {
        this.service = service;
        this.reports = reports;
        this.mapper = mapper;
    }

    @GetMapping("/reports")
    public Page<social.benji.benji_backend_api.lostfound.domain.AdvertisementReportEntity> openReports(
            @PageableDefault(size = 50) Pageable pageable) {
        return reports.findByStatusOrderByCreatedAtAsc(ReportStatus.OPEN, pageable);
    }

    @PostMapping("/ads/{publicId}/hide")
    public ManagedAdvertisementDto hide(@PathVariable UUID publicId,
                                        @RequestParam(required = false) String reason) {
        return mapper.toManaged(service.hideByAdmin(publicId, reason != null ? reason : "hidden by moderation"));
    }

    @PostMapping("/ads/{publicId}/restore")
    public ManagedAdvertisementDto restore(@PathVariable UUID publicId) {
        return mapper.toManaged(service.restoreByAdmin(publicId));
    }

    @PostMapping("/reports/{reportId}/review")
    public void review(@PathVariable UUID reportId,
                       @Valid @RequestBody ReviewReportRequest body,
                       @AuthenticationPrincipal UserPrincipal admin) {
        reports.findById(reportId).ifPresent(r -> {
            r.review(admin.id(), body.outcome(), body.note());
            reports.save(r);
        });
    }
}
