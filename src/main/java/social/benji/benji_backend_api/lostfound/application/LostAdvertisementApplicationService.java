package social.benji.benji_backend_api.lostfound.application;

import social.benji.benji_backend_api.lostfound.application.dto.CreateLostAdvertisementRequest;
import social.benji.benji_backend_api.lostfound.application.ownership.AuthenticatedUserIdResolver;
import social.benji.benji_backend_api.lostfound.application.pet.PetOwnershipValidator;
import social.benji.benji_backend_api.lostfound.domain.model.LostAdvertisement;
import social.benji.benji_backend_api.lostfound.domain.repository.LostAdvertisementRepository;
import social.benji.benji_backend_api.lostfound.domain.valueobject.Breed;
import social.benji.benji_backend_api.lostfound.domain.valueobject.Location;
import social.benji.benji_backend_api.lostfound.domain.valueobject.PetSpecies;

public class LostAdvertisementApplicationService {
    private final LostAdvertisementRepository repository;
    private final AuthenticatedUserIdResolver userIdResolver;
    private final PetOwnershipValidator petOwnershipValidator;

    public LostAdvertisementApplicationService(
            LostAdvertisementRepository repository,
            AuthenticatedUserIdResolver userIdResolver,
            PetOwnershipValidator petOwnershipValidator) {
        this.repository = repository;
        this.userIdResolver = userIdResolver;
        this.petOwnershipValidator = petOwnershipValidator;
    }

    public LostAdvertisement create(CreateLostAdvertisementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Lost advertisement request is required");
        }
        String ownerId = userIdResolver.currentUserId();
        validatePet(request.petId(), ownerId);

        LostAdvertisement advertisement = new LostAdvertisement(
                ownerId,
                request.petId(),
                PetSpecies.from(request.species()),
                Breed.of(request.breed()),
                request.petName(),
                request.physicalDescription(),
                request.distinguishingCharacteristics(),
                request.lostDate(),
                new Location(request.country(), request.city(), request.area(),
                        request.locationDescription()),
                request.contactInformation());
        return repository.save(advertisement);
    }

    private void validatePet(java.util.UUID petId, String ownerId) {
        if (petId != null && !petOwnershipValidator.validatePetOwnership(petId, ownerId)) {
            throw new IllegalArgumentException("Pet does not exist or is not owned by the authenticated user");
        }
    }
}
