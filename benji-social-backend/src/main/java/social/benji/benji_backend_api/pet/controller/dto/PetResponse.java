package social.benji.benji_backend_api.pet.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import social.benji.benji_backend_api.pet.valueobject.AcquisitionMethod;
import social.benji.benji_backend_api.pet.valueobject.ActivityLevel;
import social.benji.benji_backend_api.pet.valueobject.BodySize;
import social.benji.benji_backend_api.pet.valueobject.CoatLength;
import social.benji.benji_backend_api.pet.valueobject.FoodType;
import social.benji.benji_backend_api.pet.domain.Pet;
import social.benji.benji_backend_api.pet.valueobject.PetSex;
import social.benji.benji_backend_api.pet.valueobject.PetSpecies;
import social.benji.benji_backend_api.pet.valueobject.PetStatus;

/**
 * Full pet view for the owner. Microchip is intentionally excluded from the
 * list DTO and only visible here; public features must never read this shape.
 */
public record PetResponse(
        UUID id,
        String name,
        PetSpecies species,
        UUID breedId,
        String breedName,
        PetSex sex,
        LocalDate dateOfBirth,
        Boolean birthDateEstimated,
        java.time.Period age,
        String color,
        BigDecimal currentWeightKg,
        String microchipNumber,
        Boolean neutered,
        String coatColor,
        String city,
        String bio,
        PetStatus status,
        boolean hasPhoto,
        // Physical characteristics
        BigDecimal heightCm,
        BodySize bodySize,
        String eyeColor,
        CoatLength coatLength,
        String physicalDescription,
        // Identification
        String identificationTag,
        String registrationNumber,
        String cityLicenseNumber,
        // Behavior & lifestyle
        ActivityLevel activityLevel,
        List<String> temperament,
        String socialBehavior,
        Boolean goodWithChildren,
        Boolean goodWithOtherPets,
        String behavioralNotes,
        // Nutrition
        String dietType,
        FoodType foodType,
        String foodBrand,
        Integer dailyMealCount,
        Integer dailyFoodAmountGrams,
        String dietaryRestrictions,
        // Ownership provenance
        LocalDate acquisitionDate,
        AcquisitionMethod acquisitionMethod,
        String originDescription,
        Instant createdAt,
        Instant updatedAt) {

    public static PetResponse from(Pet pet, String breedName, boolean hasPhoto) {
        return new PetResponse(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getBreedId(),
                breedName,
                pet.getSex(),
                pet.getDateOfBirth(),
                pet.getBirthDateEstimated(),
                pet.getDateOfBirth() == null ? null : java.time.Period.between(pet.getDateOfBirth(), java.time.LocalDate.now()),
                pet.getColor(),
                pet.getCurrentWeightKg(),
                pet.getMicrochipNumber(),
                pet.getNeutered(),
                pet.getCoatColor(),
                pet.getCity(),
                pet.getBio(),
                pet.getStatus(),
                hasPhoto,
                pet.getHeightCm(),
                pet.getBodySize(),
                pet.getEyeColor(),
                pet.getCoatLength(),
                pet.getPhysicalDescription(),
                pet.getIdentificationTag(),
                pet.getRegistrationNumber(),
                pet.getCityLicenseNumber(),
                pet.getActivityLevel(),
                decodeTemperament(pet.getTemperament()),
                pet.getSocialBehavior(),
                pet.getGoodWithChildren(),
                pet.getGoodWithOtherPets(),
                pet.getBehavioralNotes(),
                pet.getDietType(),
                pet.getFoodType(),
                pet.getFoodBrand(),
                pet.getDailyMealCount(),
                pet.getDailyFoodAmountGrams(),
                pet.getDietaryRestrictions(),
                pet.getAcquisitionDate(),
                pet.getAcquisitionMethod(),
                pet.getOriginDescription(),
                pet.getCreatedAt(),
                pet.getUpdatedAt());
    }

    /** Stored as a canonical comma-separated string; exposed as a list. */
    static List<String> decodeTemperament(String stored) {
        if (stored == null || stored.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(stored.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
