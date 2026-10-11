package social.benji.benji_backend_api.petmedia.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record MovePhotoRequest(@NotNull UUID targetAlbumId) {
}
