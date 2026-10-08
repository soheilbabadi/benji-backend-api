package social.benji.benji_backend_api.pet.web.dto;

import java.util.UUID;

import social.benji.benji_backend_api.pet.domain.Breed;
import social.benji.benji_backend_api.pet.domain.Pet;
import social.benji.benji_backend_api.pet.domain.PetSpecies;

/** Lightweight shape for the "My Pets" screen — no life-record data. */
public record PetSummary(
        UUID id,
        String name,
        PetSpecies species,
        String breedName,
        String status,
        boolean hasPhoto) {

    public static PetSummary from(Pet pet, String breedName) {
        return new PetSummary(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                breedName,
                pet.getStatus().name(),
                false);
    }

    public static PetSummary of(Pet pet, String breedName, boolean hasPhoto) {
        return new PetSummary(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                breedName,
                pet.getStatus().name(),
                hasPhoto);
    }
}
