package social.benji.benji_backend_api.pet.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import social.benji.benji_backend_api.pet.domain.PetSex;

/** Null fields mean "leave unchanged"; blank strings clear optional values. */
public record UpdatePetRequest(
        @Size(min = 1, max = 100) String name,
        UUID breedId,
        PetSex sex,
        LocalDate dateOfBirth,
        @Size(max = 50) String color,
        @DecimalMin("0.0") @DecimalMax("200.0") BigDecimal weightKg,
        @Size(max = 50) String microchipNumber) {
}
