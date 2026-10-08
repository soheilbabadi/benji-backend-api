package social.benji.benji_backend_api.pet.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import social.benji.benji_backend_api.pet.domain.PetPhoto;
import social.benji.benji_backend_api.pet.domain.exception.PetErrors;
import social.benji.benji_backend_api.pet.repository.PetPhotoRepository;
import social.benji.benji_backend_api.storage.MinioProperties;
import social.benji.benji_backend_api.storage.ObjectStorage;

/**
 * Owns pet-photo business rules: validation, MinIO upload with object-key
 * generation, replacement (old photo fully removed) and access URLs. All file
 * bytes go to MinIO; PostgreSQL keeps metadata only.
 */
@Slf4j
@Service
public class PetPhotoService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");
    /** Content-type sniffing is unreliable for webp; magic bytes cover jpeg/png. */
    private static final int MAGIC_HEADER_BYTES = 12;
    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024;

    private final PetService pets;
    private final PetPhotoRepository photoRepository;
    private final ObjectStorage storage;
    private final MinioProperties minioProperties;

    public PetPhotoService(PetService pets,
                           PetPhotoRepository photoRepository,
                           ObjectStorage storage,
                           MinioProperties minioProperties) {
        this.pets = pets;
        this.photoRepository = photoRepository;
        this.storage = storage;
        this.minioProperties = minioProperties;
    }

    @Transactional
    public PetPhoto replacePhoto(UUID petId, UUID ownerId, MultipartFile file) {
        pets.requireOwnedPet(petId, ownerId);

        if (file == null || file.isEmpty()) {
            throw PetErrors.emptyPhoto();
        }
        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw PetErrors.photoTooLarge(MAX_PHOTO_BYTES);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw PetErrors.unsupportedPhotoContentType(contentType);
        }
        validateMagicBytes(file, contentType);

        // Upload-first: a failed DB commit may orphan an object in MinIO, but a
        // DB row pointing at missing bytes would be worse. Orphans are cleaned
        // operationally, not by distributed transactions.
        UUID photoId = UUID.randomUUID();
        String objectKey = "pets/" + petId + "/photos/" + photoId + extensionFor(contentType);
        try (InputStream in = file.getInputStream()) {
            storage.put(minioProperties.bucketName(), objectKey, in, file.getSize(), contentType);
        } catch (IOException e) {
            log.warn("Failed to read upload stream for pet {}", petId);
            throw PetErrors.emptyPhoto();
        }

        PetPhoto photo = PetPhoto.builder()
                .id(photoId)
                .petId(petId)
                .objectKey(objectKey)
                .contentType(contentType)
                .fileSizeBytes(file.getSize())
                .primary(true)
                .build();
        PetPhoto saved = photoRepository.save(photo);

        removePreviousPhotos(petId, photoId);
        return saved;
    }

    /** Short-lived pre-signed URL so private objects are never publicly readable. */
    @Transactional(readOnly = true)
    public String getPhotoAccessUrl(UUID petId, UUID ownerId) {
        pets.requireOwnedPet(petId, ownerId);
        PetPhoto photo = photoRepository.findFirstByPetIdAndPrimaryIsTrue(petId)
                .orElseThrow(PetErrors::photoNotFound);
        return storage.presignedGetUrl(
                minioProperties.bucketName(),
                photo.getObjectKey(),
                java.time.Duration.ofMinutes(minioProperties.presignExpiryMinutes()));
    }

    @Transactional
    public void deletePhoto(UUID petId, UUID ownerId) {
        pets.requireOwnedPet(petId, ownerId);
        for (PetPhoto photo : photoRepository.findByPetId(petId)) {
            storage.delete(minioProperties.bucketName(), photo.getObjectKey());
            photoRepository.delete(photo);
        }
    }

    private void removePreviousPhotos(UUID petId, UUID keepId) {
        photoRepository.findByPetId(petId).stream()
                .filter(p -> !p.getId().equals(keepId))
                .forEach(p -> {
                    storage.delete(minioProperties.bucketName(), p.getObjectKey());
                    photoRepository.delete(p);
                });
    }

    private void validateMagicBytes(MultipartFile file, String contentType) {
        byte[] header = new byte[MAGIC_HEADER_BYTES];
        try (InputStream in = file.getInputStream()) {
            int read = in.read(header);
            if (read < 3) {
                throw PetErrors.emptyPhoto();
            }
        } catch (IOException e) {
            throw PetErrors.emptyPhoto();
        }
        boolean matches = switch (contentType.toLowerCase()) {
            case "image/jpeg" -> (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8;
            case "image/png" -> (header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G';
            case "image/webp" -> header.length >= 12
                    && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
            default -> false;
        };
        if (!matches) {
            throw PetErrors.unsupportedPhotoContentType(contentType);
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
