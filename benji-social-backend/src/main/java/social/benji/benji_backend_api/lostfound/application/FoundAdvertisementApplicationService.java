package social.benji.benji_backend_api.lostfound.application;

import social.benji.benji_backend_api.lostfound.application.dto.CreateFoundAdvertisementRequest;
import social.benji.benji_backend_api.lostfound.application.ownership.AuthenticatedUserIdResolver;
import social.benji.benji_backend_api.lostfound.application.pet.PetOwnershipValidator;
import social.benji.benji_backend_api.lostfound.domain.model.FoundAdvertisement;
import social.benji.benji_backend_api.lostfound.domain.repository.FoundAdvertisementRepository;
import social.benji.benji_backend_api.lostfound.domain.valueobject.Breed;
import social.benji.benji_backend_api.lostfound.domain.valueobject.Location;
import social.benji.benji_backend_api.lostfound.domain.valueobject.PetSpecies;

public class FoundAdvertisementApplicationService {
    private final FoundAdvertisementRepository repository;
    private final AuthenticatedUserIdResolver userIdResolver;
    private final PetOwnershipValidator petOwnershipValidator;

    public FoundAdvertisementApplicationService(
            FoundAdvertisementRepository repository,
            AuthenticatedUserIdResolver userIdResolver,
            PetOwnershipValidator petOwnershipValidator) {
        this.repository = repository;
        this.userIdResolver = userIdResolver;
        this.petOwnershipValidator = petOwnershipValidator;
    }

    public FoundAdvertisement create(CreateFoundAdvertisementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Found advertisement request is required");
        }
        String ownerId = userIdResolver.currentUserId();
        validatePet(request.petId(), ownerId);

        FoundAdvertisement advertisement = new FoundAdvertisement(
                ownerId,
                request.petId(),
                PetSpecies.from(request.species()),
                Breed.of(request.breed()),
                request.physicalDescription(),
                request.distinguishingCharacteristics(),
                request.foundDate(),
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
