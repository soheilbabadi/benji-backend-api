package social.benji.benji_backend_api.lostfound.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.lostfound.domain.*;
import social.benji.benji_backend_api.lostfound.repository.AdvertisementReportRepository;
import social.benji.benji_backend_api.lostfound.repository.ContactRevealLogRepository;
import social.benji.benji_backend_api.lostfound.repository.LostFoundAdvertisementRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Core lifecycle & query service. Ownership is always derived from the authenticated principal. */
@Service
public class LostFoundAdvertisementService {

    private final LostFoundAdvertisementRepository advertisements;
    private final AdvertisementReportRepository reports;
    private final ContactRevealLogRepository revealLogs;
    private final PetOwnershipPort petOwnership;

    public LostFoundAdvertisementService(LostFoundAdvertisementRepository advertisements,
                                        AdvertisementReportRepository reports,
                                        ContactRevealLogRepository revealLogs,
                                        PetOwnershipPort petOwnership) {
        this.advertisements = advertisements;
        this.reports = reports;
        this.revealLogs = revealLogs;
        this.petOwnership = petOwnership;
    }

    // ---- Creation ------------------------------------------------------------------------

    /** LOST: requires an owned pet (server-side check). Saved as DRAFT; payment comes later. */
    @Transactional
    public LostFoundAdvertisementEntity createLost(UUID advertiserId, LostFoundAdvertisementEntity draft) {
        if (!petOwnership.ownsPet(draft.getPetId(), advertiserId)) {
            throw new BenjiCustomException.ValidationException("Pet not found or not owned by user");
        }
        draft.setAdvertisementType(AdvertisementType.LOST);
        draft.setUserId(advertiserId);
        draft.setStatus(AdvertisementStatus.DRAFT);
        draft.assertTypeConsistency();
        return advertisements.save(draft);
    }

    /** FOUND: never references a pet, never requires payment. Activated immediately on validation. */
    @Transactional
    public LostFoundAdvertisementEntity createFound(UUID finderId, LostFoundAdvertisementEntity draft) {
        draft.setAdvertisementType(AdvertisementType.FOUND);
        draft.setPetId(null);
        draft.setUserId(finderId);
        draft.setStatus(AdvertisementStatus.DRAFT);
        draft.assertTypeConsistency();
        LostFoundAdvertisementEntity saved = advertisements.save(draft);
        saved.transitionTo(AdvertisementStatus.ACTIVE); // FOUND flow: no moderation gate in MVP
        return advertisements.save(saved);
    }

    // ---- Payment submission (LOST only) ----------------------------------------------------

    /**
     * Moves a DRAFT LOST ad into PAYMENT_PENDING. The actual payment transaction and amount are
     * owned by the Payment module (this call passes only serviceType + ad id); the client can
     * NEVER activate an ad by submitting a status or transaction id.
     */
    @Transactional
    public LostFoundAdvertisementEntity submitForPayment(UUID advertiserId, UUID publicAdId) {
        LostFoundAdvertisementEntity ad = requireOwned(advertiserId, publicAdId);
        if (ad.getAdvertisementType() != AdvertisementType.LOST) {
            throw new BenjiCustomException.ValidationException("Only LOST advertisements require payment");
        }
        if (advertisements.existsByPetIdAndStatus(ad.getPetId(), AdvertisementStatus.PAYMENT_PENDING)
                && ad.getStatus() != AdvertisementStatus.PAYMENT_PENDING) {
            throw new BenjiCustomException.DuplicateEntityException(
                    "Another advertisement for this pet is already awaiting payment");
        }
        ad.transitionTo(AdvertisementStatus.PAYMENT_PENDING);
        return advertisements.save(ad);
    }

    // ---- Queries -----------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<LostFoundAdvertisementEntity> searchPublic(List<AdvertisementStatus> visibleStatuses,
                                                           AdvertisementType type, Species species,
                                                           String city, String area, UUID breedId,
                                                           AnimalSex sex, Instant from, Instant to,
                                                           String text, Pageable pageable) {
        return advertisements.searchPublic(visibleStatuses, type, species, city, area, breedId,
                sex, from, to, text, pageable);
    }

    @Transactional(readOnly = true)
    public LostFoundAdvertisementEntity getPublic(UUID publicAdId) {
        LostFoundAdvertisementEntity ad = advertisements.findByPublicId(publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Advertisement not found"));
        if (!ad.getStatus().isPubliclyVisible()) {
            throw new BenjiCustomException.ResourceNotFoundException("Advertisement not found"); // no state leak
        }
        return ad;
    }

    @Transactional(readOnly = true)
    public Page<LostFoundAdvertisementEntity> myAdvertisements(UUID userId, Pageable pageable) {
        return advertisements.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public LostFoundAdvertisementEntity getManaged(UUID advertiserId, UUID publicAdId) {
        return requireOwned(advertiserId, publicAdId);
    }

    // ---- Lifecycle actions (owner-initiated) ----------------------------------------------------

    @Transactional
    public LostFoundAdvertisementEntity resolve(UUID userId, UUID publicAdId, String note) {
        LostFoundAdvertisementEntity ad = requireOwned(userId, publicAdId);
        ad.markResolved(userId, note); // RESOLVED is terminal — reactivation is impossible (approved)
        return advertisements.save(ad);
    }

    @Transactional
    public LostFoundAdvertisementEntity cancelOrClose(UUID userId, UUID publicAdId) {
        LostFoundAdvertisementEntity ad = requireOwned(userId, publicAdId);
        ad.transitionTo(ad.getStatus() == AdvertisementStatus.DRAFT
                || ad.getStatus() == AdvertisementStatus.PAYMENT_PENDING
                ? AdvertisementStatus.CANCELLED : AdvertisementStatus.CLOSED);
        return advertisements.save(ad);
    }

    @Transactional
    public LostFoundAdvertisementEntity updateDraft(UUID userId, UUID publicAdId,
                                                    java.util.function.Consumer<LostFoundAdvertisementEntity> editor) {
        LostFoundAdvertisementEntity ad = requireOwned(userId, publicAdId);
        if (!ad.getStatus().isEditable()) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Advertisement cannot be edited in status " + ad.getStatus());
        }
        editor.accept(ad);
        ad.assertTypeConsistency();
        return advertisements.save(ad);
    }

    // ---- Contact reveal (mask/reveal privacy model) ----------------------------------------------

    /** Returns the full phone ONLY after logging the reveal; abuse-windowed per viewer+ad/IP. */
    @Transactional
    public String revealContact(UUID publicAdId, UUID viewerUserId, String viewerIp) {
        LostFoundAdvertisementEntity ad = getPublic(publicAdId);
        if (ad.getContactMethod() != ContactMethod.PHONE || ad.getContactPhoneE164() == null) {
            throw new BenjiCustomException.ValidationException("Advertiser did not offer phone contact");
        }
        Instant dayAgo = Instant.now().minus(java.time.Duration.ofDays(1));
        long mine = viewerUserId != null ? revealLogs.countByViewerUserIdAndCreatedAtAfter(viewerUserId, dayAgo) : 0;
        long sameAdIp = revealLogs.countByAdvertisementIdAndViewerIpAndCreatedAtAfter(ad.getId(), viewerIp, dayAgo);
        if (mine >= 20 || sameAdIp >= 5) {
            throw new BenjiCustomException.ResourceForbiddenException("Contact reveal limit reached, try later");
        }
        revealLogs.save(ContactRevealLogEntity.builder()
                .advertisementId(ad.getId()).viewerUserId(viewerUserId).viewerIp(viewerIp).build());
        return ad.getContactPhoneE164();
    }

    // ---- Moderation (minimal MVP) ------------------------------------------------------------------

    @Transactional
    public void report(UUID reporterUserId, UUID publicAdId, ReportReason reason, String details) {
        LostFoundAdvertisementEntity ad = getPublic(publicAdId);
        if (ad.isOwnedBy(reporterUserId)) {
            throw new BenjiCustomException.ValidationException("Cannot report your own advertisement");
        }
        if (reports.existsByAdvertisementIdAndReporterUserIdAndStatus(
                ad.getId(), reporterUserId, ReportStatus.OPEN)) {
            return; // duplicate open report → idempotent success
        }
        reports.save(AdvertisementReportEntity.builder()
                .advertisementId(ad.getId()).reporterUserId(reporterUserId)
                .reason(reason).details(details).build());
    }

    /** Admin-only entry point (controller enforces ROLE_ADMIN). */
    @Transactional
    public LostFoundAdvertisementEntity hideByAdmin(UUID publicAdId, String reason) {
        LostFoundAdvertisementEntity ad = advertisements.findByPublicId(publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Advertisement not found"));
        ad.hide(reason);
        return advertisements.save(ad);
    }

    @Transactional
    public LostFoundAdvertisementEntity restoreByAdmin(UUID publicAdId) {
        LostFoundAdvertisementEntity ad = advertisements.findByPublicId(publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Advertisement not found"));
        ad.transitionTo(AdvertisementStatus.ACTIVE);
        return advertisements.save(ad);
    }

    // ---- helpers --------------------------------------------------------------------------------------

    private LostFoundAdvertisementEntity requireOwned(UUID advertiserId, UUID publicAdId) {
        LostFoundAdvertisementEntity ad = advertisements.findByPublicId(publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Advertisement not found"));
        if (!ad.isOwnedBy(advertiserId)) {
            throw new BenjiCustomException.ResourceUnauthorizedException("Not your advertisement");
        }
        return ad;
    }
}
