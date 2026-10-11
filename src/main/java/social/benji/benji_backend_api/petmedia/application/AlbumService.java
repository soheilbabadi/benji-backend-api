package social.benji.benji_backend_api.petmedia.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import social.benji.benji_backend_api.petmedia.domain.PetAlbum;
import social.benji.benji_backend_api.petmedia.domain.PetAlbumMedia;
import social.benji.benji_backend_api.petmedia.domain.exception.MediaErrors;
import social.benji.benji_backend_api.petmedia.repository.PetAlbumMediaRepository;
import social.benji.benji_backend_api.petmedia.repository.PetAlbumRepository;
import social.benji.benji_backend_api.storage.MinioProperties;
import social.benji.benji_backend_api.storage.ObjectStorage;

/**
 * Album lifecycle: create, list, update, hard delete. Deletion removes every
 * photo's MinIO object and DB row — the approved MVP behavior for albums.
 */
@Slf4j
@Service
public class AlbumService {

    private final OwnershipGuard guard;
    private final PetAlbumRepository albums;
    private final PetAlbumMediaRepository media;
    private final ObjectStorage storage;
    private final MinioProperties minio;

    public AlbumService(OwnershipGuard guard,
                        PetAlbumRepository albums,
                        PetAlbumMediaRepository media,
                        ObjectStorage storage,
                        MinioProperties minio) {
        this.guard = guard;
        this.albums = albums;
        this.media = media;
        this.storage = storage;
        this.minio = minio;
    }

    @Transactional
    public PetAlbum createAlbum(UUID petId, UUID ownerId, String name, String description) {
        guard.requireOwnedPet(petId, ownerId);
        return albums.save(PetAlbum.builder()
                .id(UUID.randomUUID())
                .petId(petId)
                .name(name.trim())
                .description(blankToNull(description))
                .build());
    }

    @Transactional(readOnly = true)
    public List<PetAlbum> listAlbums(UUID petId, UUID ownerId) {
        guard.requireOwnedPet(petId, ownerId);
        return albums.findByPetIdOrderByCreatedAtDesc(petId);
    }

    @Transactional(readOnly = true)
    public PetAlbum getAlbum(UUID petId, UUID albumId, UUID ownerId) {
        return guard.requireOwnedAlbum(petId, albumId, ownerId);
    }

    @Transactional
    public PetAlbum updateAlbum(UUID petId, UUID albumId, UUID ownerId, String name, String description) {
        PetAlbum album = guard.requireOwnedAlbum(petId, albumId, ownerId);
        if (name != null && !name.isBlank()) {
            album.setName(name.trim());
        }
        if (description != null) {
            album.setDescription(blankToNull(description));
        }
        return albums.save(album);
    }

    /** Hard delete: photos are removed from MinIO first so no orphan outlives its album. */
    @Transactional
    public void deleteAlbum(UUID petId, UUID albumId, UUID ownerId) {
        PetAlbum album = guard.requireOwnedAlbum(petId, albumId, ownerId);
        for (PetAlbumMedia photo : media.findByAlbumIdOrderByCreatedAtDesc(album.getId())) {
            try {
                storage.delete(minio.bucketName(), photo.getObjectKey());
            } catch (RuntimeException e) {
                // Fail fast: an album whose bytes we cannot remove must not be dropped,
                // otherwise the object becomes unreachable garbage without a pointer.
                log.error("MinIO delete failed for {} during album deletion", photo.getObjectKey(), e);
                throw e;
            }
            media.delete(photo);
        }
        albums.delete(album);
    }

    @Transactional
    public PetAlbum setCover(UUID petId, UUID albumId, UUID ownerId, UUID coverPhotoId) {
        PetAlbum album = guard.requireOwnedAlbum(petId, albumId, ownerId);
        PetAlbumMedia cover = media.findById(coverPhotoId).orElseThrow(MediaErrors::photoNotFound);
        if (!cover.getAlbumId().equals(album.getId())) {
            throw MediaErrors.coverNotInAlbum();
        }
        album.setCoverPhotoId(cover.getId());
        return albums.save(album);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
