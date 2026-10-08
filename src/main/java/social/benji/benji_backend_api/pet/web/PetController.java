package social.benji.benji_backend_api.pet.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import social.benji.benji_backend_api.pet.domain.Breed;
import social.benji.benji_backend_api.pet.domain.Pet;
import social.benji.benji_backend_api.pet.domain.PetPhoto;
import social.benji.benji_backend_api.pet.domain.PetSpecies;
import social.benji.benji_backend_api.pet.repository.BreedRepository;
import social.benji.benji_backend_api.pet.repository.PetPhotoRepository;
import social.benji.benji_backend_api.pet.service.PetPhotoService;
import social.benji.benji_backend_api.pet.service.PetService;
import social.benji.benji_backend_api.pet.web.dto.BreedResponse;
import social.benji.benji_backend_api.pet.web.dto.CreatePetRequest;
import social.benji.benji_backend_api.pet.web.dto.PetResponse;
import social.benji.benji_backend_api.pet.web.dto.PetSummary;
import social.benji.benji_backend_api.pet.web.dto.UpdatePetRequest;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

/**
 * Thin REST layer. The authenticated principal — never a client-provided owner
 * id — determines ownership; all rules live in the services.
 */
@RestController
@RequestMapping("/api/v1/pets")
public class PetController {

    private final PetService petService;
    private final PetPhotoService petPhotoService;
    private final BreedRepository breedRepository;
    private final PetPhotoRepository petPhotoRepository;

    public PetController(PetService petService,
                         PetPhotoService petPhotoService,
                         BreedRepository breedRepository,
                         PetPhotoRepository petPhotoRepository) {
        this.petService = petService;
        this.petPhotoService = petPhotoService;
        this.breedRepository = breedRepository;
        this.petPhotoRepository = petPhotoRepository;
    }

    @PostMapping
    public ResponseEntity<PetResponse> create(@Valid @RequestBody CreatePetRequest request,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        Pet pet = petService.create(principal.id(), request.name(), request.species(),
                request.breedId(), request.sex(), request.dateOfBirth(),
                request.color(), request.weightKg(), request.microchipNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(pet));
    }

    /** Default list shows active pets only; archived/deceased via ?includeArchived=true. */
    @GetMapping
    public List<PetSummary> list(@RequestParam(defaultValue = "false") boolean includeArchived,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        List<Pet> pets = petService.listMyPets(principal.id(), includeArchived);
        Map<UUID, String> breedNames = breedNames(pets);
        return pets.stream()
                .map(pet -> PetSummary.of(pet, breedName(breedNames, pet), hasPhoto(pet.getId())))
                .toList();
    }

    @GetMapping("/{petId}")
    public PetResponse get(@PathVariable UUID petId,
                           @AuthenticationPrincipal UserPrincipal principal) {
        return toResponse(petService.getOwnedPet(petId, principal.id()));
    }

    @PatchMapping("/{petId}")
    public PetResponse update(@PathVariable UUID petId,
                              @Valid @RequestBody UpdatePetRequest request,
                              @AuthenticationPrincipal UserPrincipal principal) {
        Pet pet = petService.update(petId, principal.id(), request.name(), request.breedId(),
                request.sex(), request.dateOfBirth(), request.color(),
                request.weightKg(), request.microchipNumber());
        return toResponse(pet);
    }

    /** Maps DELETE to archive: physical deletion would destroy the life record. */
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> archive(@PathVariable UUID petId,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        petService.archive(petId, principal.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{petId}/restore")
    public PetResponse restore(@PathVariable UUID petId,
                               @AuthenticationPrincipal UserPrincipal principal) {
        petService.restore(petId, principal.id());
        return toResponse(petService.getOwnedPet(petId, principal.id()));
    }

    @PostMapping("/{petId}/deceased")
    public PetResponse markDeceased(@PathVariable UUID petId,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        petService.markDeceased(petId, principal.id());
        return toResponse(petService.getOwnedPet(petId, principal.id()));
    }

    /** Replacing the profile photo fully removes the previous one (MinIO + DB). */
    @PutMapping(value = "/{petId}/photo", consumes = "multipart/form-data")
    public ResponseEntity<Void> uploadPhoto(@PathVariable UUID petId,
                                            @RequestPart("file") MultipartFile file,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        petPhotoService.replacePhoto(petId, principal.id(), file);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{petId}/photo-url")
    public Map<String, String> photoUrl(@PathVariable UUID petId,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("url", petPhotoService.getPhotoAccessUrl(petId, principal.id()));
    }

    @DeleteMapping("/{petId}/photo")
    public ResponseEntity<Void> deletePhoto(@PathVariable UUID petId,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        petPhotoService.deletePhoto(petId, principal.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/breeds")
    public List<BreedResponse> breeds(@RequestParam PetSpecies species,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return petService.listBreeds(species).stream().map(BreedResponse::from).toList();
    }

    private PetResponse toResponse(Pet pet) {
        String breedName = pet.getBreedId() == null
                ? null
                : breedRepository.findById(pet.getBreedId()).map(Breed::getName).orElse(null);
        return PetResponse.from(pet, breedName, hasPhoto(pet.getId()));
    }

    private boolean hasPhoto(UUID petId) {
        return petPhotoRepository.findFirstByPetIdAndPrimaryIsTrue(petId).isPresent();
    }

    private Map<UUID, String> breedNames(List<Pet> pets) {
        List<UUID> ids = pets.stream().map(Pet::getBreedId).filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return breedRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Breed::getId, Breed::getName));
    }

    private String breedName(Map<UUID, String> names, Pet pet) {
        return pet.getBreedId() == null ? null : names.get(pet.getBreedId());
    }
}
