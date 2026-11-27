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
import social.benji.benji_backend_api.lostfound.repository.AdvertisementImageRepository;
import social.benji.benji_backend_api.lostfound.repository.AdvertisementRejectionHistoryRepository;
import social.benji.benji_backend_api.lostfound.repository.AdvertisementModerationHistoryRepository;
import social.benji.benji_backend_api.lostfound.repository.AdvertisementDeletionCleanupRepository;
import social.benji.benji_backend_api.lostfound.repository.PaymentSucceededProcessingRepository;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementDeletionCleanupEntity;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementDeletionStatus;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementRejectionHistoryEntity;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementModerationHistoryEntity;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementModerationOperation;
import social.benji.benji_backend_api.storage.ObjectStorage;
import social.benji.benji_backend_api.storage.MinioProperties;
import social.benji.benji_backend_api.lostfound.application.payment.LostPaymentPreparation;
import social.benji.benji_backend_api.payment.application.dto.CreatePaymentRequest;

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
    private final AdvertisementRejectionHistoryRepository rejectionHistory;
    private final AdvertisementModerationHistoryRepository moderationHistory;
    private final AdvertisementImageRepository images;
    private final AdvertisementDeletionCleanupRepository deletionCleanup;
    private final PaymentSucceededProcessingRepository paymentSucceededProcessing;
    private final ObjectStorage objectStorage;
    private final MinioProperties minio;

    public LostFoundAdvertisementService(LostFoundAdvertisementRepository advertisements,
                                        AdvertisementReportRepository reports,
                                        ContactRevealLogRepository revealLogs,
                                        PetOwnershipPort petOwnership,
                                        AdvertisementRejectionHistoryRepository rejectionHistory,
                                        AdvertisementModerationHistoryRepository moderationHistory,
                                        AdvertisementImageRepository images,
                                        AdvertisementDeletionCleanupRepository deletionCleanup,
                                        PaymentSucceededProcessingRepository paymentSucceededProcessing,
                                        ObjectStorage objectStorage,
                                        MinioProperties minio) {
        this.advertisements = advertisements;
        this.reports = reports;
        this.revealLogs = revealLogs;
        this.petOwnership = petOwnership;
        this.rejectionHistory = rejectionHistory;
        this.moderationHistory = moderationHistory;
        this.images = images;
        this.deletionCleanup = deletionCleanup;
        this.paymentSucceededProcessing = paymentSucceededProcessing;
        this.objectStorage = objectStorage;
        this.minio = minio;
    }

    // ---- Creation ------------------------------------------------------------------------

    /** LOST: requires an owned pet (server-side check). Saved under independent LOST review lifecycle. */
    @Transactional
    public LostFoundAdvertisementEntity createLost(UUID advertiserId, LostFoundAdvertisementEntity draft) {
        if (!petOwnership.ownsPet(draft.getPetId(), advertiserId)) {
            throw new BenjiCustomException.ValidationException("Pet not found or not owned by user");
        }
        draft.setAdvertisementType(AdvertisementType.LOST);
        draft.setUserId(advertiserId);
        draft.enterModeration();
        draft.assertTypeConsistency();
        return advertisements.save(draft);
    }

    /** FOUND: never references a pet and enters independent moderation without Payment. */
    @Transactional
    public LostFoundAdvertisementEntity createFound(UUID finderId, LostFoundAdvertisementEntity draft) {
        draft.setAdvertisementType(AdvertisementType.FOUND);
        draft.setPetId(null);
        draft.setUserId(finderId);
        draft.enterModeration();
        draft.assertTypeConsistency();
        return advertisements.save(draft);
    }

    // ---- Independent moderation -------------------------------------------------------------

    @Transactional
    public LostFoundAdvertisementEntity reject(UUID administratorId, UUID publicAdId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BenjiCustomException.ValidationException("Rejection reason is required");
        }
        LostFoundAdvertisementEntity ad = advertisements.findByPublicIdForUpdate(publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Advertisement not found"));
        ad.rejectLostOrFound(Instant.now());
        long sequence = rejectionHistory.countByAdvertisementId(ad.getId()) + 1;
        rejectionHistory.save(AdvertisementRejectionHistoryEntity.builder()
                .advertisementId(ad.getId())
                .advertisementType(ad.getAdvertisementType())
                .administratorUserId(administratorId)
                .rejectionReason(reason.trim())
                .rejectionSequence((int) sequence)
                .rejectedAt(Instant.now())
                .build());
        moderationHistory.save(AdvertisementModerationHistoryEntity.builder()
                .advertisementId(ad.getId())
                .advertisementType(ad.getAdvertisementType())
                .administratorUserId(administratorId)
                .operationType(AdvertisementModerationOperation.REJECTED)
                .reason(reason.trim())
                .operatedAt(Instant.now())
                .build());
        return advertisements.save(ad);
    }

    @Transactional
    public LostFoundAdvertisementEntity approve(UUID administratorId, UUID publicAdId) {
        LostFoundAdvertisementEntity ad = advertisements.findByPublicIdForUpdate(publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Advertisement not found"));
        Instant now = Instant.now();
        if (ad.getAdvertisementType() == AdvertisementType.LOST) {
            ad.approveLost(now);
        } else {
            ad.approveFound(now);
        }
        moderationHistory.save(AdvertisementModerationHistoryEntity.builder()
                .advertisementId(ad.getId())
                .advertisementType(ad.getAdvertisementType())
                .administratorUserId(administratorId)
                .operationType(AdvertisementModerationOperation.APPROVED)
                .operatedAt(now)
                .build());
        return advertisements.save(ad);
    }

    @Transactional
    public LostFoundAdvertisementEntity resubmit(UUID advertiserId, UUID publicAdId) {
        LostFoundAdvertisementEntity ad = requireOwnedForUpdate(advertiserId, publicAdId);
        ad.resubmit(Instant.now());
        return advertisements.save(ad);
    }

    @Transactional(readOnly = true)
    public CreatePaymentRequest prepareLostPayment(UUID advertiserId, UUID publicAdId,
                                                   String idempotencyKey) {
        LostFoundAdvertisementEntity ad = requireOwned(advertiserId, publicAdId);
        if (ad.getAdvertisementType() != AdvertisementType.LOST
                || ad.getStatus() != AdvertisementStatus.PAYMENT_PENDING) {
            throw new BenjiCustomException.ValidationException(
                    "Lost advertisement is not approved and awaiting payment");
        }
        if (ad.getPaymentDeadlineAt() == null
                || !Instant.now().isBefore(ad.getPaymentDeadlineAt())) {
            throw new BenjiCustomException.ValidationException(
                    "Lost advertisement payment deadline has elapsed");
        }
        return LostPaymentPreparation.prepare(ad.getPublicId(), idempotencyKey);
    }

    // ---- Queries -----------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<LostFoundAdvertisementEntity> searchPublic(List<AdvertisementStatus> visibleStatuses,
                                                           AdvertisementType type, Species species,
                                                           String city, String area, UUID breedId,
                                                           AnimalSex sex, Instant from, Instant to,
                                                           String text, Pageable pageable) {
        return advertisements.searchPublic(visibleStatuses, Instant.now(), type, species, city,
                area, breedId, sex, from, to, text, pageable);
    }

    @Transactional(readOnly = true)
    public LostFoundAdvertisementEntity getPublic(UUID publicAdId) {
        LostFoundAdvertisementEntity ad = advertisements.findByPublicId(publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Advertisement not found"));
        if (ad.getStatus() != AdvertisementStatus.PUBLISHED
                || ad.getExpiresAt() == null
                || !ad.getExpiresAt().isAfter(Instant.now())) {
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
        LostFoundAdvertisementEntity ad = advertisements.findByPublicIdForUpdate(publicAdId).orElse(null);
        if (ad == null) {
            deletionCleanup.findById(publicAdId).ifPresent(job -> {
                if (!job.getOwnerUserId().equals(userId)) {
                    throw new BenjiCustomException.ResourceUnauthorizedException("Not your advertisement");
                }
            });
            return null;
        }
        if (!ad.isOwnedBy(userId)) {
            throw new BenjiCustomException.ResourceUnauthorizedException("Not your advertisement");
        }
        if (ad.getStatus() != AdvertisementStatus.PUBLISHED) {
            throw new BenjiCustomException.ValidationException("Only published advertisements can be resolved");
        }
        ad.markResolved(userId, note);
        permanentDelete(ad);
        return null;
    }

    @Transactional
    public LostFoundAdvertisementEntity cancelOrClose(UUID userId, UUID publicAdId) {
        LostFoundAdvertisementEntity ad = advertisements.findByPublicIdForUpdate(publicAdId).orElse(null);
        if (ad == null) {
            deletionCleanup.findById(publicAdId).ifPresent(job -> {
                if (!job.getOwnerUserId().equals(userId)) {
                    throw new BenjiCustomException.ResourceUnauthorizedException("Not your advertisement");
                }
            });
            return null;
        }
        if (!ad.isOwnedBy(userId)) {
            throw new BenjiCustomException.ResourceUnauthorizedException("Not your advertisement");
        }
        permanentDelete(ad);
        return null;
    }

    @Transactional
    public LostFoundAdvertisementEntity updateDraft(UUID userId, UUID publicAdId,
                                                    java.util.function.Consumer<LostFoundAdvertisementEntity> editor) {
        LostFoundAdvertisementEntity ad = requireOwnedForUpdate(userId, publicAdId);
        if (ad.getStatus() != AdvertisementStatus.REJECTED) {
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
    public LostFoundAdvertisementEntity hideByAdmin(UUID administratorId, UUID publicAdId, String reason) {
        LostFoundAdvertisementEntity ad = advertisements.findByPublicIdForUpdate(publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Advertisement not found"));
        Instant now = Instant.now();
        ad.deactivate(administratorId, reason, now);
        moderationHistory.save(AdvertisementModerationHistoryEntity.builder()
                .advertisementId(ad.getId())
                .advertisementType(ad.getAdvertisementType())
                .administratorUserId(administratorId)
                .operationType(AdvertisementModerationOperation.DEACTIVATED)
                .reason(reason.trim())
                .operatedAt(now)
                .build());
        return advertisements.save(ad);
    }

    public LostFoundAdvertisementEntity restoreByAdmin(UUID publicAdId) {
        throw new BenjiCustomException.ValidationException(
                "Expired or deactivated advertisements cannot be restored");
    }

    @Transactional
    public void permanentDelete(LostFoundAdvertisementEntity ad) {
        if (deletionCleanup.existsById(ad.getPublicId())) {
            return;
        }
        List<String> objectKeys = images.findByAdvertisementIdOrderBySortOrderAsc(ad.getPublicId())
                .stream()
                .map(social.benji.benji_backend_api.lostfound.domain.AdvertisementImageEntity::getObjectKey)
                .toList();
        deletionCleanup.save(AdvertisementDeletionCleanupEntity.builder()
                .advertisementPublicId(ad.getPublicId())
                .ownerUserId(ad.getUserId())
                .imageObjectKeys(String.join("\n", objectKeys))
                .status(AdvertisementDeletionStatus.PENDING)
                .build());
        UUID databaseId = ad.getId();
        UUID publicId = ad.getPublicId();
        advertisements.delete(ad);
        images.deleteByAdvertisementId(publicId);
        rejectionHistory.deleteByAdvertisementId(databaseId);
        moderationHistory.deleteByAdvertisementId(databaseId);
        reports.deleteByAdvertisementId(databaseId);
        revealLogs.deleteByAdvertisementId(databaseId);
        paymentSucceededProcessing.deleteByAdvertisementPublicId(publicId);
    }

    public void permanentDeleteFromScheduler(LostFoundAdvertisementEntity ad) {
        permanentDelete(ad);
    }

    @Transactional
    public void deleteExpiredOrDeactivatedByAdmin(UUID administratorId, UUID publicAdId) {
        LostFoundAdvertisementEntity ad =
                advertisements.findByPublicIdForUpdate(publicAdId).orElse(null);
        if (ad == null) {
            return;
        }
        if (ad.getStatus() != AdvertisementStatus.EXPIRED
                && ad.getStatus() != AdvertisementStatus.DEACTIVATED) {
            throw new BenjiCustomException.ValidationException(
                    "Only expired or deactivated advertisements can be administratively deleted");
        }
        permanentDelete(ad);
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

    private LostFoundAdvertisementEntity requireOwnedForUpdate(UUID advertiserId, UUID publicAdId) {
        LostFoundAdvertisementEntity ad = advertisements.findByPublicIdForUpdate(publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Advertisement not found"));
        if (!ad.isOwnedBy(advertiserId)) {
            throw new BenjiCustomException.ResourceUnauthorizedException("Not your advertisement");
        }
        return ad;
    }
}
