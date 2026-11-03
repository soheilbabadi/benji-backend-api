package social.benji.benji_backend_api.pet.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import social.benji.benji_backend_api.pet.domain.BodySize;
import social.benji.benji_backend_api.pet.domain.CoatLength;

/** Section 2 of the profile. Null fields stay unchanged; blanks clear values. */
public record UpdatePhysicalRequest(
        @DecimalMin("0.0") @DecimalMax("200.0") BigDecimal currentWeightKg,
        @DecimalMin("0.1") @DecimalMax("300.0") BigDecimal heightCm,
        BodySize bodySize,
        @Size(max = 50) String eyeColor,
        CoatLength coatLength,
        @Size(max = 1000) String physicalDescription) {
}
