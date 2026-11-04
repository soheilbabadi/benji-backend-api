package social.benji.benji_backend_api.pet.controller.dto;

import jakarta.validation.constraints.Size;

/** Section 3 of the profile. Null fields stay unchanged; blanks clear values. */
public record UpdateIdentificationRequest(
        @Size(max = 50) String microchipNumber,
        @Size(max = 100) String identificationTag,
        @Size(max = 100) String registrationNumber,
        @Size(max = 100) String cityLicenseNumber) {
}
