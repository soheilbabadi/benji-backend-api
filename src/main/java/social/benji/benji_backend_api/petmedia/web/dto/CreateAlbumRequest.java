package social.benji.benji_backend_api.petmedia.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Album creation needs only a name; photos are added later. */
public record CreateAlbumRequest(
        @NotBlank @Size(min = 1, max = 100) String name,
        @Size(max = 500) String description) {
}
