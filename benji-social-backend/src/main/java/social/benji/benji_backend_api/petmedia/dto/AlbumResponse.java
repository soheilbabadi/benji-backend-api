package social.benji.benji_backend_api.petmedia.dto;

import social.benji.benji_backend_api.petmedia.domain.PetAlbum;

import java.time.Instant;
import java.util.UUID;

/** No bucket/objectKey exposure: clients only see ids and a cover reference. */
public record AlbumResponse(
        UUID id,
        UUID petId,
        String petName,
        String description,
        UUID coverPhotoId,
        Instant createdAt,
        Instant updatedAt) {

    public static AlbumResponse from(PetAlbum album) {
        return new AlbumResponse(album.getId(), album.getPetId(), album.getPetName(),
                album.getDescription(), album.getCoverPhotoId(),
                album.getCreatedAt(), album.getUpdatedAt());
    }
}
