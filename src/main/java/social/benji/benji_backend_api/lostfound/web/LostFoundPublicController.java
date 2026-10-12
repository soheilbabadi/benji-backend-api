package social.benji.benji_backend_api.lostfound.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementStatus;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementType;
import social.benji.benji_backend_api.lostfound.domain.AnimalSex;
import social.benji.benji_backend_api.lostfound.domain.Species;
import social.benji.benji_backend_api.lostfound.service.AdvertisementImageService;
import social.benji.benji_backend_api.lostfound.service.LostFoundAdvertisementService;
import social.benji.benji_backend_api.lostfound.web.dto.LostFoundDtos.*;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public browsing endpoints under /api/public/** (permitAll in SecurityConfig). Reads are open;
 * POST /report and the contact reveal require authentication where noted. Anonymous GETs never
 * see unmasked phone numbers or owner identifiers.
 */
@RestController
@RequestMapping("/api/public/lost-found")
class LostFoundPublicController {

    private final LostFoundAdvertisementService service;
    private final AdvertisementImageService imageService;
    private final LostFoundMapper mapper;

    LostFoundPublicController(LostFoundAdvertisementService service,
                              AdvertisementImageService imageService,
                              LostFoundMapper mapper) {
        this.service = service;
        this.imageService = imageService;
        this.mapper = mapper;
    }

    /**
     * Search visible ads. includeInactive=true adds RESOLVED/EXPIRED to results.
     * All filters optional; pagination via standard Spring Pageable params.
     */
    @GetMapping("/ads")
    public Page<PublicAdvertisementDto> search(
            @RequestParam(required = false) AdvertisementType type,
            @RequestParam(required = false) Species species,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) UUID breedId,
            @RequestParam(required = false) AnimalSex sex,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @PageableDefault(size = 20) Pageable pageable) {

        List<AdvertisementStatus> statuses = includeInactive
                ? List.of(AdvertisementStatus.ACTIVE, AdvertisementStatus.RESOLVED, AdvertisementStatus.EXPIRED)
                : List.of(AdvertisementStatus.ACTIVE);
        return service.searchPublic(statuses, type, species, city, area, breedId, sex, from, to, q, pageable)
                .map(mapper::toPublic);
    }

    @GetMapping("/ads/{publicId}")
    public PublicAdvertisementDto getAd(@PathVariable UUID publicId) {
        return mapper.toPublic(service.getPublic(publicId));
    }

    /** Image delivery: returns a short-lived presigned URL for one private-bucket object. */
    @GetMapping("/ads/{publicId}/images/{imageId}")
    public ResponseEntity<Void> imageUrl(@PathVariable UUID publicId, @PathVariable UUID imageId) {
        String url = imageService.publicImageUrl(publicId, imageId);
        return ResponseEntity.status(302)
                .header("Location", url)
                .header("Cache-Control", "no-store")
                .build();
    }

    /**
     * Masked contact stays on the ad DTO; this is the explicit interaction that reveals it.
     * Requires authentication (anonymous users must sign in first — deters scraping) and every
     * reveal is audit-logged with rate limiting.
     */
    @PostMapping("/ads/{publicId}/contact")
    public ContactRevealResponse revealContact(@PathVariable UUID publicId,
                                               @AuthenticationPrincipal Object principal,
                                               HttpServletRequest request) {
        UUID viewerId = principal instanceof UserPrincipal up ? up.id() : null;
        if (viewerId == null) {
            throw new BenjiCustomException.ResourceUnauthorizedException("Sign in to view contact details");
        }
        String ip = clientIp(request);
        String phone = service.revealContact(publicId, viewerId, ip);
        return new ContactRevealResponse(phone,
                "Please mention you found this ad on Benji. Do not share this number further.");
    }

    /** Minimal MVP moderation entry point (approved: implement minimal POST /report). */
    @PostMapping("/ads/{publicId}/report")
    public ResponseEntity<Void> report(@PathVariable UUID publicId,
                                       @Valid @RequestBody ReportRequest body,
                                       @AuthenticationPrincipal Object principal) {
        UUID reporterId = principal instanceof UserPrincipal up ? up.id() : null;
        if (reporterId == null) {
            throw new BenjiCustomException.ResourceUnauthorizedException("Sign in to report an advertisement");
        }
        service.report(reporterId, publicId, body.reason(), body.details());
        return ResponseEntity.accepted().build(); // idempotent: duplicate open reports → same 202
    }

    private static String clientIp(HttpServletRequest req) {
        return Optional.ofNullable(req.getHeader("X-Forwarded-For"))
                .map(f -> f.split(",")[0].trim())
                .orElse(req.getRemoteAddr());
    }
}
