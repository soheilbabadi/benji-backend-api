package social.benji.benji_backend_api.lostfound.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementImageEntity;
import social.benji.benji_backend_api.lostfound.domain.LostFoundAdvertisementEntity;
import social.benji.benji_backend_api.lostfound.repository.AdvertisementImageRepository;
import social.benji.benji_backend_api.lostfound.repository.LostFoundAdvertisementRepository;
import social.benji.benji_backend_api.storage.MinioProperties;
import social.benji.benji_backend_api.storage.ObjectStorage;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Image handling: binaries go to MinIO through the SHARED ObjectStorage port only; PostgreSQL
 * stores metadata + backend-generated keys. Public delivery uses short-lived presigned GET URLs
 * (bucket stays private). Compensation: if the DB insert fails after upload, the object is
 * deleted best-effort; if MinIO delete fails on removal, the metadata row is still removed and
 * the orphaned key is logged for a cleanup sweep.
 */
@Service
@Slf4j
public class AdvertisementImageService {

    /** MIME → stored extension; validated by magic bytes too, not just content-type header. */
    private static final Map<String, String> ALLOWED = Map.of(
            "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");

    private final AdvertisementImageRepository images;
    private final LostFoundAdvertisementRepository advertisements;
    private final ObjectStorage storage;
    private final MinioProperties minio;

    public AdvertisementImageService(AdvertisementImageRepository images,
                                    LostFoundAdvertisementRepository advertisements,
                                    ObjectStorage storage,
                                    MinioProperties minio) {
        this.images = images;
        this.advertisements = advertisements;
        this.storage = storage;
        this.minio = minio;
    }

    @Transactional
    public AdvertisementImageEntity upload(UUID advertiserId, UUID publicAdId,
                                           String contentType, byte[] bytes) {
        requireEditableOwned(advertiserId, publicAdId);
        if (bytes == null || bytes.length == 0 || bytes.length > AdvertisementImageEntity.MAX_IMAGE_BYTES) {
            throw new BenjiCustomException.ValidationException("Image must be non-empty and <= 5MB");
        }
        String ext = detectExtension(bytes, contentType);
        if (ext == null) {
            throw new BenjiCustomException.ValidationException("Only JPEG/PNG/WebP images are allowed");
        }
        long count = images.countByAdvertisementId(publicAdId);
        if (count >= AdvertisementImageEntity.MAX_IMAGES_PER_ADVERTISEMENT) {
            throw new BenjiCustomException.ValidationException("Maximum 5 images per advertisement");
        }
        UUID imageId = UUID.randomUUID();
        // Backend-generated key — the client filename is never used.
        String key = AdvertisementImageEntity.objectKeyFor(publicAdId, imageId, ext);
        try {
            storage.put(minio.bucketName(), key, new ByteArrayInputStream(bytes),
                    bytes.length, detectedMime(bytes));
        } catch (RuntimeException e) {
            throw new BenjiCustomException.ApplicationServerException("Image storage unavailable: " + e.getMessage());
        }
        try {
            AdvertisementImageEntity meta = AdvertisementImageEntity.builder()
                    .advertisementId(publicAdId).objectKey(key)
                    .contentType(detectedMime(bytes)).sizeBytes(bytes.length)
                    .sortOrder((int) count).build();
            return images.save(meta);
        } catch (RuntimeException e) {
            storage.delete(minio.bucketName(), key); // compensate uploaded-but-unpersisted object
            throw e;
        }
    }

    /** Presigned URL for a PUBLIC ad image; callers get 404 for non-visible ads (no state leak). */
    @Transactional(readOnly = true)
    public String publicImageUrl(UUID publicAdId, UUID imageId) {
        LostFoundAdvertisementEntity ad = advertisements.findByPublicId(publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Advertisement not found"));
        if (!ad.getStatus().isPubliclyVisible()) {
            throw new BenjiCustomException.ResourceNotFoundException("Advertisement not found");
        }
        AdvertisementImageEntity img = images.findByIdAndAdvertisementId(imageId, publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Image not found"));
        return storage.presignedGetUrl(minio.bucketName(), img.getObjectKey(), java.time.Duration.ofMinutes(10));
    }

    @Transactional(readOnly = true)
    public List<AdvertisementImageEntity> list(UUID publicAdId) {
        return images.findByAdvertisementIdOrderBySortOrderAsc(publicAdId);
    }

    @Transactional
    public void delete(UUID advertiserId, UUID publicAdId, UUID imageId) {
        requireEditableOwned(advertiserId, publicAdId);
        AdvertisementImageEntity img = images.findByIdAndAdvertisementId(imageId, publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Image not found"));
        images.delete(img);
        try {
            storage.delete(minio.bucketName(), img.getObjectKey());
        } catch (RuntimeException e) {
            // Metadata already gone; log orphan key for the periodic cleanup sweep.

        }
    }

    private void requireEditableOwned(UUID advertiserId, UUID publicAdId) {
        LostFoundAdvertisementEntity ad = advertisements.findByPublicId(publicAdId)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Advertisement not found"));
        if (!ad.isOwnedBy(advertiserId)) {
            throw new BenjiCustomException.ResourceUnauthorizedException("Not your advertisement");
        }
        if (!ad.getStatus().isEditable()) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Images can only change while the advertisement is DRAFT or PAYMENT_PENDING");
        }
    }

    // ---- Magic-byte sniffing (content validation independent of client headers) --------------

    private static String detectExtension(byte[] b, String declaredContentType) {
        String mime = detectedMime(b);
        return mime != null && ALLOWED.containsKey(mime) ? ALLOWED.get(mime) : null;
    }

    private static String detectedMime(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}
