package social.benji.benji_backend_api.petmedia.application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import social.benji.benji_backend_api.petmedia.domain.MediaType;
import social.benji.benji_backend_api.petmedia.domain.PetAlbum;
import social.benji.benji_backend_api.petmedia.domain.PetAlbumMedia;
import social.benji.benji_backend_api.petmedia.domain.exception.MediaErrors;
import social.benji.benji_backend_api.petmedia.repository.PetAlbumMediaRepository;
import social.benji.benji_backend_api.petmedia.repository.PetAlbumRepository;
import social.benji.benji_backend_api.storage.MinioProperties;
import social.benji.benji_backend_api.storage.ObjectStorage;

/**
 * Photo metadata + MinIO object management inside albums. Uploads are validated
 * and normalized server-side (resize cap + JPEG re-encode), dimensions are
 * extracted by the backend, and moving between albums only re-points the DB row
 * — the stored object never moves.
 */
@Slf4j
@Service
public class PhotoService {

    private static final int MAX_DIMENSION_PX = 2048;
    private static final float JPEG_QUALITY = 0.85f;
    private static final Set<String> OUTPUT_CAPABLE_TYPES = Set.of("image/jpeg", "image/png");
    static final int MAX_FILES_PER_REQUEST = 20;

    private final OwnershipGuard guard;
    private final PetAlbumRepository albums;
    private final PetAlbumMediaRepository photos;
    private final ImageValidator validator;
    private final ObjectStorage storage;
    private final MinioProperties minio;

    public PhotoService(OwnershipGuard guard,
                        PetAlbumRepository albums,
                        PetAlbumMediaRepository photos,
                        ImageValidator validator,
                        ObjectStorage storage,
                        MinioProperties minio) {
        this.guard = guard;
        this.albums = albums;
        this.photos = photos;
        this.validator = validator;
        this.storage = storage;
        this.minio = minio;
    }

    /**
     * Upload-first strategy: bytes land in MinIO before the DB row is written.
     * A failed commit may orphan an object (cleaned operationally); a DB row
     * pointing at missing bytes would be worse.
     */
    @Transactional
    public List<PetAlbumMedia> uploadPhotos(UUID petId, UUID albumId, UUID ownerId, List<MultipartFile> files) {
        PetAlbum album = guard.requireOwnedAlbum(petId, albumId, ownerId);
        if (files == null || files.isEmpty()) {
            throw MediaErrors.emptyFile();
        }
        if (files.size() > MAX_FILES_PER_REQUEST) {
            throw MediaErrors.tooManyFiles(MAX_FILES_PER_REQUEST);
        }

        List<PetAlbumMedia> saved = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            byte[] original = validator.validate(file);
            StoredImage processed = normalize(original, file.getContentType().toLowerCase());
            ImageValidator.Dimensions dims = validator.detectDimensions(processed.bytes());

            UUID photoId = UUID.randomUUID();
            String objectKey = "pets/" + petId + "/albums/" + album.getId() + "/" + photoId + "." + processed.extension();
            try {
                storage.put(minio.bucketName(), objectKey,
                        new ByteArrayInputStream(processed.bytes()), processed.bytes().length,
                        processed.contentType());
            } catch (RuntimeException e) {
                // Nothing was persisted for this file; surface a clean error.
                log.warn("MinIO upload failed for album {}", album.getId(), e);
                throw e;
            }

            saved.add(photos.save(PetAlbumMedia.builder()
                    .id(photoId)
                    .albumId(album.getId())
                    .mediaType(MediaType.PHOTO)
                    .objectKey(objectKey)
                    .contentType(processed.contentType())
                    .fileSizeBytes((long) processed.bytes().length)
                    .widthPx(dims != null ? dims.width() : null)
                    .heightPx(dims != null ? dims.height() : null)
                    .build()));
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<PetAlbumMedia> listPhotos(UUID petId, UUID albumId, UUID ownerId) {
        PetAlbum album = guard.requireOwnedAlbum(petId, albumId, ownerId);
        return photos.findByAlbumIdOrderByCreatedAtDesc(album.getId());
    }

    /** Short-lived pre-signed GET URL; bucket policy stays private. */
    @Transactional(readOnly = true)
    public String getAccessUrl(UUID petId, UUID albumId, UUID photoId, UUID ownerId) {
        requireOwnedPhoto(petId, albumId, photoId, ownerId);
        PetAlbumMedia photo = photos.findById(photoId).orElseThrow(MediaErrors::photoNotFound);
        return storage.presignedGetUrl(minio.bucketName(), photo.getObjectKey(),
                Duration.ofMinutes(minio.presignExpiryMinutes()));
    }

    @Transactional
    public void deletePhoto(UUID petId, UUID albumId, UUID photoId, UUID ownerId) {
        PetAlbum album = requireOwnedPhoto(petId, albumId, photoId, ownerId);
        PetAlbumMedia photo = photos.findById(photoId).orElseThrow(MediaErrors::photoNotFound);

        storage.delete(minio.bucketName(), photo.getObjectKey());
        photos.delete(photo);

        if (photoId.equals(album.getCoverPhotoId())) {
            album.setCoverPhotoId(null);
            albums.save(album);
        }
    }

    /** Logical move only: the MinIO object keeps its original key under the source album. */
    @Transactional
    public PetAlbumMedia movePhoto(UUID petId, UUID sourceAlbumId, UUID photoId, UUID targetAlbumId, UUID ownerId) {
        requireOwnedPhoto(petId, sourceAlbumId, photoId, ownerId);
        PetAlbum target = guard.requireOwnedAlbum(petId, targetAlbumId, ownerId);
        PetAlbumMedia photo = photos.findById(photoId).orElseThrow(MediaErrors::photoNotFound);
        photo.setAlbumId(target.getId());
        return photos.save(photo);
    }

    private PetAlbum requireOwnedPhoto(UUID petId, UUID albumId, UUID photoId, UUID ownerId) {
        PetAlbum album = guard.requireOwnedAlbum(petId, albumId, ownerId);
        PetAlbumMedia photo = photos.findById(photoId).orElseThrow(MediaErrors::photoNotFound);
        if (!photo.getAlbumId().equals(album.getId())) {
            throw MediaErrors.photoNotFound();
        }
        return album;
    }

    record StoredImage(byte[] bytes, String contentType, String extension) {
    }

    /**
     * Resize cap (max side 2048px) + format normalization to JPEG for png/webp
     * inputs. webp cannot be decoded by ImageIO, so it is stored as-is after
     * validation; jpeg/png go through the pipeline.
     */
    StoredImage normalize(byte[] input, String declaredContentType) {
        if ("image/webp".equals(declaredContentType)) {
            return new StoredImage(input, "image/webp", "webp");
        }
        try {
            var image = ImageIO.read(new ByteArrayInputStream(input));
            if (image == null) {
                throw MediaErrors.corruptedImage();
            }
            var resized = downscale(image);
            byte[] jpeg = encodeJpeg(resized);
            return new StoredImage(jpeg, "image/jpeg", "jpg");
        } catch (IOException e) {
            throw MediaErrors.corruptedImage();
        }
    }

    private java.awt.image.BufferedImage downscale(java.awt.image.BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= MAX_DIMENSION_PX && h <= MAX_DIMENSION_PX) {
            return src;
        }
        double scale = Math.min((double) MAX_DIMENSION_PX / w, (double) MAX_DIMENSION_PX / h);
        int nw = Math.max(1, (int) Math.round(w * scale));
        int nh = Math.max(1, (int) Math.round(h * scale));
        var out = new java.awt.image.BufferedImage(nw, nh, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var g = out.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        return out;
    }

    private byte[] encodeJpeg(java.awt.image.BufferedImage image) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(JPEG_QUALITY);
        var bos = new java.io.ByteArrayOutputStream();
        try (var out = new MemoryCacheImageOutputStream(bos)) {
            writer.setOutput(out);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
        return bos.toByteArray();
    }
}
