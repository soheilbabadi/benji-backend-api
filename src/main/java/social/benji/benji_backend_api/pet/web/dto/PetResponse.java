package social.benji.benji_backend_api.pet.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import social.benji.benji_backend_api.pet.domain.Pet;
import social.benji.benji_backend_api.pet.domain.PetSex;
import social.benji.benji_backend_api.pet.domain.PetSpecies;
import social.benji.benji_backend_api.pet.domain.PetStatus;

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
                pet.getCreatedAt(),
                pet.getUpdatedAt());
    }
}
