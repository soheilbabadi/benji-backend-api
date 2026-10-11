package social.benji.benji_backend_api.petmedia.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SetCoverRequest(@NotNull UUID photoId) {
}
