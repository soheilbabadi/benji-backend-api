package social.benji.benji_backend_api.lostfound.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateFoundAdvertisementRequest(
        UUID petId,
        @NotBlank @Pattern(regexp = "(?i)^(DOG|CAT)$")
        String species,
        @Size(max = 100)
        String breed,
        @NotBlank @Size(max = 2000)
        String physicalDescription,
        @Size(max = 1000)
        String distinguishingCharacteristics,
        @NotNull
        LocalDate foundDate,
        @NotBlank @Size(max = 100)
        String country,
        @NotBlank @Size(max = 100)
        String city,
        @Size(max = 100)
        String area,
        @Size(max = 500)
        String locationDescription,
        @Size(max = 500)
        String contactInformation
) {}
