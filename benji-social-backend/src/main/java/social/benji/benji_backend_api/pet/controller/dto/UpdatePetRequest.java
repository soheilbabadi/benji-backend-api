package social.benji.benji_backend_api.pet.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import social.benji.benji_backend_api.pet.valueobject.AcquisitionMethod;
import social.benji.benji_backend_api.pet.valueobject.ActivityLevel;
import social.benji.benji_backend_api.pet.valueobject.BodySize;
import social.benji.benji_backend_api.pet.valueobject.CoatLength;
import social.benji.benji_backend_api.pet.valueobject.FoodType;
import social.benji.benji_backend_api.pet.valueobject.PetSex;

/**
 * Legacy whole-profile PATCH kept for backward compatibility. Null fields mean
 * "leave unchanged"; blank strings clear optional values. New clients should
 * use the section-specific PATCH DTOs.
 */
public record UpdatePetRequest(
        @Size(min = 1, max = 100) String name,
        UUID breedId,
        PetSex sex,
        LocalDate dateOfBirth,
        Boolean birthDateEstimated,
        @Size(max = 50) String color,
        @DecimalMin("0.0") @DecimalMax("200.0") BigDecimal weightKg,
        @Size(max = 50) String microchipNumber,
        Boolean neutered,
        @Size(max = 50) String coatColor,
        @Size(max = 100) String city,
        @Size(max = 500) String bio,
        @DecimalMin("0.1") @DecimalMax("300.0") BigDecimal heightCm,
        BodySize bodySize,
        @Size(max = 50) String eyeColor,
        CoatLength coatLength,
        @Size(max = 1000) String physicalDescription,
        @Size(max = 100) String identificationTag,
        @Size(max = 100) String registrationNumber,
        @Size(max = 100) String cityLicenseNumber,
        ActivityLevel activityLevel,
        @Size(max = 20) List<@Size(max = 50) String> temperament,
        @Size(max = 500) String socialBehavior,
        Boolean goodWithChildren,
        Boolean goodWithOtherPets,
        @Size(max = 1000) String behavioralNotes,
        @Size(max = 100) String dietType,
        FoodType foodType,
        @Size(max = 100) String foodBrand,
        @Min(1) @Max(12) Integer dailyMealCount,
        @Min(1) @Max(100000) Integer dailyFoodAmountGrams,
        @Size(max = 500) String dietaryRestrictions,
        LocalDate acquisitionDate,
        AcquisitionMethod acquisitionMethod,
        @Size(max = 500) String originDescription) {
}
