package social.benji.benji_backend_api.pet.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import social.benji.benji_backend_api.pet.domain.AcquisitionMethod;
import social.benji.benji_backend_api.pet.domain.ActivityLevel;
import social.benji.benji_backend_api.pet.domain.BodySize;
import social.benji.benji_backend_api.pet.domain.CoatLength;
import social.benji.benji_backend_api.pet.domain.FoodType;
import social.benji.benji_backend_api.pet.domain.PetSex;
import social.benji.benji_backend_api.pet.domain.PetSpecies;

/**
 * Only name and species (a.k.a. petType: DOG or CAT) are required — every
 * other attribute can be completed later (progressive profile completion).
 * All optional groups mirror the five profile sections managed by PATCH.
 */
public record CreatePetRequest(
        @NotBlank @Size(min = 1, max = 100) String name,
        @NotNull PetSpecies species,
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

        // Physical characteristics
        @DecimalMin("0.1") @DecimalMax("300.0") BigDecimal heightCm,
        BodySize bodySize,
        @Size(max = 50) String eyeColor,
        CoatLength coatLength,
        @Size(max = 1000) String physicalDescription,

        // Identification
        @Size(max = 100) String identificationTag,
        @Size(max = 100) String registrationNumber,
        @Size(max = 100) String cityLicenseNumber,

        // Behavior & lifestyle
        ActivityLevel activityLevel,
        @Size(max = 20) List<@Size(max = 50) String> temperament,
        @Size(max = 500) String socialBehavior,
        Boolean goodWithChildren,
        Boolean goodWithOtherPets,
        @Size(max = 1000) String behavioralNotes,

        // Nutrition
        @Size(max = 100) String dietType,
        FoodType foodType,
        @Size(max = 100) String foodBrand,
        @Min(1) @Max(12) Integer dailyMealCount,
        @Min(1) @Max(100000) Integer dailyFoodAmountGrams,
        @Size(max = 500) String dietaryRestrictions,

        // Ownership provenance
        LocalDate acquisitionDate,
        AcquisitionMethod acquisitionMethod,
        @Size(max = 500) String originDescription) {
}
