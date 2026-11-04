package social.benji.benji_backend_api.pet.controller.dto;

import java.util.UUID;

import jakarta.validation.constraints.Size;
import social.benji.benji_backend_api.pet.domain.PetSex;

/**
 * PATCH semantics: absent (null) fields are left unchanged; a blank string
 * clears an optional value — the project's established convention.
 */
public record UpdateBasicInfoRequest(
        @Size(min = 1, max = 100) String name,
        UUID breedId,
        PetSex sex,
        java.time.LocalDate dateOfBirth,
        Boolean birthDateEstimated,
        @Size(max = 50) String coatColor,
        Boolean neutered,
        @Size(max = 50) String color,
        @Size(max = 100) String city,
        @Size(max = 500) String bio) {
}
