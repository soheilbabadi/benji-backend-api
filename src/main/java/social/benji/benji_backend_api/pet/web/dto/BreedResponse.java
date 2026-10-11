package social.benji.benji_backend_api.pet.web.dto;

import java.util.UUID;

import social.benji.benji_backend_api.pet.domain.Breed;

public record BreedResponse(UUID id, String name, boolean mixed, boolean unknown) {

    public static BreedResponse from(Breed breed) {
        return new BreedResponse(breed.getId(), breed.getName(), breed.isMixed(), breed.isUnknown());
    }
}
