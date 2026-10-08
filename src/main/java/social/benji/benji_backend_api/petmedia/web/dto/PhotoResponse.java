package social.benji.benji_backend_api.petmedia.web.dto;

import java.time.Instant;
import java.util.UUID;

import social.benji.benji_backend_api.petmedia.domain.MediaType;
import social.benji.benji_backend_api.petmedia.domain.PetAlbumMedia;

/** Media descriptor for the UI; access bytes come via the separate url endpoint. */
public record PhotoResponse(
        UUID id,
        UUID albumId,
        MediaType type,
        String contentType,
        long size,
        Integer width,
        Integer height,
        Instant createdAt) {

    public static PhotoResponse from(PetAlbumMedia photo) {
        return new PhotoResponse(photo.getId(), photo.getAlbumId(), photo.getMediaType(),
                photo.getContentType(), photo.getFileSizeBytes(),
                photo.getWidthPx(), photo.getHeightPx(), photo.getCreatedAt());
    }
}
