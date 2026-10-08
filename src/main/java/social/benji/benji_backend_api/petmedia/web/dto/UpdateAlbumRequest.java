package social.benji.benji_backend_api.petmedia.web.dto;

import jakarta.validation.constraints.Size;

/** Partial update: null fields are left untouched. */
public record UpdateAlbumRequest(
        @Size(min = 1, max = 100) String name,
        @Size(max = 500) String description) {
}
