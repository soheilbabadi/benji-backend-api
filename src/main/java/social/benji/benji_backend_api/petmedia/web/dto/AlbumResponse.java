package social.benji.benji_backend_api.petmedia.web.dto;

import java.time.Instant;
import java.util.UUID;

import social.benji.benji_backend_api.petmedia.domain.PetAlbum;

/** No bucket/objectKey exposure: clients only see ids and a cover reference. */
public record AlbumResponse(
        UUID id,
        UUID petId,
        String name,
        String description,
        UUID coverPhotoId,
        Instant createdAt,
        Instant updatedAt) {

    public static AlbumResponse from(PetAlbum album) {
        return new AlbumResponse(album.getId(), album.getPetId(), album.getName(),
                album.getDescription(), album.getCoverPhotoId(),
                album.getCreatedAt(), album.getUpdatedAt());
    }
}
