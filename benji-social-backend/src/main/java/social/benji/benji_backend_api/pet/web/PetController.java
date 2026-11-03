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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import social.benji.benji_backend_api.pet.domain.Breed;
import social.benji.benji_backend_api.pet.domain.Pet;
import social.benji.benji_backend_api.pet.domain.PetPhoto;
import social.benji.benji_backend_api.pet.domain.PetSpecies;
import social.benji.benji_backend_api.pet.repository.BreedRepository;
import social.benji.benji_backend_api.pet.repository.PetPhotoRepository;
import social.benji.benji_backend_api.pet.service.PetPhotoService;
import social.benji.benji_backend_api.pet.service.PetProfileService;
import social.benji.benji_backend_api.pet.service.PetService;
import social.benji.benji_backend_api.pet.web.dto.BreedResponse;
import social.benji.benji_backend_api.pet.web.dto.CreatePetRequest;
import social.benji.benji_backend_api.pet.web.dto.PetResponse;
import social.benji.benji_backend_api.pet.web.dto.PetSummary;
import social.benji.benji_backend_api.pet.web.dto.UpdateBasicInfoRequest;
import social.benji.benji_backend_api.pet.web.dto.UpdateBehaviorRequest;
import social.benji.benji_backend_api.pet.web.dto.UpdateIdentificationRequest;
import social.benji.benji_backend_api.pet.web.dto.UpdateNutritionRequest;
import social.benji.benji_backend_api.pet.web.dto.UpdatePetRequest;
import social.benji.benji_backend_api.pet.web.dto.UpdatePhysicalRequest;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

/**
 * Thin REST layer. The authenticated principal — never a client-provided owner
 * id — determines ownership; all rules live in the services.
 */
@RestController
@RequestMapping("/api/v1/pets")
@Tag(name = "Pets", description = "Pet profile management: registration, progressive section updates, lifecycle status and profile photo")
public class PetController {

    private static final String AUTH_REQUIRED = "Requires a Bearer JWT; every operation is scoped to the authenticated owner (foreign pets appear as 404).";

    private final PetService petService;
    private final PetProfileService petProfileService;
    private final PetPhotoService petPhotoService;
    private final BreedRepository breedRepository;
    private final PetPhotoRepository petPhotoRepository;

    public PetController(PetService petService,
                         PetProfileService petProfileService,
                         PetPhotoService petPhotoService,
                         BreedRepository breedRepository,
                         PetPhotoRepository petPhotoRepository) {
        this.petService = petService;
        this.petProfileService = petProfileService;
        this.petPhotoService = petPhotoService;
        this.breedRepository = breedRepository;
        this.petPhotoRepository = petPhotoRepository;
    }

    @Operation(summary = "Create a pet profile",
            description = "Only name and species (DOG/CAT) are required; every other attribute may be completed later. "
                    + AUTH_REQUIRED)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pet created"),
            @ApiResponse(responseCode = "400", description = "Validation error (name/species/weight/breed mismatch/enum)")
    })
    @PostMapping
    public ResponseEntity<PetResponse> create(@Valid @RequestBody CreatePetRequest request,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        Pet pet = petProfileService.create(principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(pet));
    }

    @Operation(summary = "List my pets",
            description = "Default list shows active pets only; archived/deceased/lost/found/rehomed via ?includeArchived=true.")
    @ApiResponse(responseCode = "200", description = "Pet summaries for the authenticated owner")
    @GetMapping
    public List<PetSummary> list(@RequestParam(defaultValue = "false") boolean includeArchived,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        List<Pet> pets = petService.listMyPets(principal.id(), includeArchived);
        Map<UUID, String> breedNames = breedNames(pets);
        return pets.stream()
                .map(pet -> PetSummary.of(pet, breedName(breedNames, pet), hasPhoto(pet.getId())))
                .toList();
    }

    @Operation(summary = "Get a pet profile", description = AUTH_REQUIRED)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Full pet profile"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller")
    })
    @GetMapping("/{petId}")
    public PetResponse get(@Parameter(description = "Pet id") @PathVariable UUID petId,
                           @AuthenticationPrincipal UserPrincipal principal) {
        return toResponse(petService.getOwnedPet(petId, principal.id()));
    }

    /**
     * Legacy whole-profile PATCH kept for backward compatibility. Null fields
     * stay unchanged; blank strings clear optional values. New clients should
     * prefer the section-specific endpoints below.
     */
    @Operation(summary = "Update a pet profile (legacy whole-profile PATCH)",
            description = "PATCH semantics: null = unchanged, blank string clears optional text. "
                    + "Rejected with 409 when the pet is DECEASED or REHOMED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pet updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "Terminal status: profile frozen")
    })
    @PatchMapping("/{petId}")
    public PetResponse update(@PathVariable UUID petId,
                              @Valid @RequestBody UpdatePetRequest request,
                              @AuthenticationPrincipal UserPrincipal principal) {
        Pet pet = petService.update(petId, principal.id(), request.name(), request.breedId(),
                request.sex(), request.dateOfBirth(), request.color(),
                request.weightKg(), request.microchipNumber(),
                request.neutered(), request.coatColor(),
                request.city(), request.bio());
        applyLegacyExtras(pet, request);
        return toResponse(pet);
    }

    @Operation(summary = "Update basic information section",
            description = "Section 1 of the profile. Null = unchanged; blank string clears optional text.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated pet profile"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g. INVALID_BREED, future date of birth)"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "INVALID_PET_STATUS_TRANSITION: terminal status")
    })
    @PatchMapping("/{petId}/basic-info")
    public PetResponse updateBasicInfo(@PathVariable UUID petId,
                                       @Valid @RequestBody UpdateBasicInfoRequest request,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        return toResponse(petProfileService.updateBasicInfo(petId, principal.id(), request));
    }

    @Operation(summary = "Update physical characteristics section",
            description = "Section 2. currentWeightKg is a snapshot only; weight history is a separate module.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated pet profile"),
            @ApiResponse(responseCode = "400", description = "Validation error (INVALID_WEIGHT, INVALID_HEIGHT)"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "Terminal status: profile frozen")
    })
    @PatchMapping("/{petId}/physical")
    public PetResponse updatePhysical(@PathVariable UUID petId,
                                      @Valid @RequestBody UpdatePhysicalRequest request,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return toResponse(petProfileService.updatePhysical(petId, principal.id(), request));
    }

    @Operation(summary = "Update identification section",
            description = "Section 3. Microchip numbers are normalized (spaces/dashes stripped) and must match [A-Za-z0-9]{10,15}.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated pet profile"),
            @ApiResponse(responseCode = "400", description = "Validation error (INVALID_MICROCHIP_NUMBER)"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "Terminal status: profile frozen")
    })
    @PatchMapping("/{petId}/identification")
    public PetResponse updateIdentification(@PathVariable UUID petId,
                                            @Valid @RequestBody UpdateIdentificationRequest request,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        return toResponse(petProfileService.updateIdentification(petId, principal.id(), request));
    }

    @Operation(summary = "Update behavior & lifestyle section",
            description = "Section 4. A non-null temperament list REPLACES the stored tags; an empty list clears them.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated pet profile"),
            @ApiResponse(responseCode = "400", description = "Validation error (INVALID_TEMPERAMENT_TAG)"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "Terminal status: profile frozen")
    })
    @PatchMapping("/{petId}/behavior")
    public PetResponse updateBehavior(@PathVariable UUID petId,
                                      @Valid @RequestBody UpdateBehaviorRequest request,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return toResponse(petProfileService.updateBehavior(petId, principal.id(), request));
    }

    @Operation(summary = "Update nutrition section",
            description = "Section 5. Daily meal count and food amount must be positive when provided.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated pet profile"),
            @ApiResponse(responseCode = "400", description = "Validation error (INVALID_MEAL_COUNT, INVALID_FOOD_AMOUNT)"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "Terminal status: profile frozen")
    })
    @PatchMapping("/{petId}/nutrition")
    public PetResponse updateNutrition(@PathVariable UUID petId,
                                       @Valid @RequestBody UpdateNutritionRequest request,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        return toResponse(petProfileService.updateNutrition(petId, principal.id(), request));
    }

    /** Maps DELETE to archive: physical deletion would destroy the life record. */
    @Operation(summary = "Archive a pet", description = "Soft delete: moves the profile to ARCHIVED. Physical deletion is not supported.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pet archived"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "Terminal status: cannot change state")
    })
    @DeleteMapping("/{petId}")
    public ResponseEntity<HttpStatus> archive(@PathVariable UUID petId,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        petService.archive(petId, principal.id());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore an archived pet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pet restored to ACTIVE"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "Pet is not ARCHIVED")
    })
    @PostMapping("/{petId}/restore")
    public PetResponse restore(@PathVariable UUID petId,
                               @AuthenticationPrincipal UserPrincipal principal) {
        petService.restore(petId, principal.id());
        return toResponse(petService.getOwnedPet(petId, principal.id()));
    }

    @Operation(summary = "Mark pet as deceased", description = "Terminal state: the profile becomes a permanent, read-only record.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status set to DECEASED"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "Pet already in a terminal status")
    })
    @PostMapping("/{petId}/deceased")
    public PetResponse markDeceased(@PathVariable UUID petId,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        petService.markDeceased(petId, principal.id());
        return toResponse(petService.getOwnedPet(petId, principal.id()));
    }

    @Operation(summary = "Mark pet as LOST",
            description = "Profile marker only (ACTIVE or FOUND -> LOST). It does NOT create or modify any Lost & Found advertisement, which has its own independent state machine.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status set to LOST"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "INVALID_PET_STATUS_TRANSITION from current status")
    })
    @PostMapping("/{petId}/status/lost")
    public PetResponse markLost(@PathVariable UUID petId,
                                @AuthenticationPrincipal UserPrincipal principal) {
        petProfileService.markLost(petId, principal.id());
        return toResponse(petService.getOwnedPet(petId, principal.id()));
    }

    @Operation(summary = "Mark pet as FOUND",
            description = "Profile marker only (LOST or FOUND -> FOUND). Independent from Lost & Found advertisements.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status set to FOUND"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "INVALID_PET_STATUS_TRANSITION from current status")
    })
    @PostMapping("/{petId}/status/found")
    public PetResponse markFound(@PathVariable UUID petId,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        petProfileService.markFound(petId, principal.id());
        return toResponse(petService.getOwnedPet(petId, principal.id()));
    }

    @Operation(summary = "Recover a lost/found pet", description = "LOST or FOUND -> ACTIVE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status set to ACTIVE"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "INVALID_PET_STATUS_TRANSITION from current status")
    })
    @PostMapping("/{petId}/status/recovered")
    public PetResponse markRecovered(@PathVariable UUID petId,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        petProfileService.markRecovered(petId, principal.id());
        return toResponse(petService.getOwnedPet(petId, principal.id()));
    }

    @Operation(summary = "Mark pet as REHOMED", description = "Terminal state: the profile freezes like DECEASED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status set to REHOMED"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller"),
            @ApiResponse(responseCode = "409", description = "Pet already in a terminal status")
    })
    @PostMapping("/{petId}/status/rehomed")
    public PetResponse markRehomed(@PathVariable UUID petId,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        petProfileService.markRehomed(petId, principal.id());
        return toResponse(petService.getOwnedPet(petId, principal.id()));
    }

    /** Replacing the profile photo fully removes the previous one (MinIO + DB). */
    @Operation(summary = "Upload or replace the profile photo",
            description = "multipart/form-data image upload; the previous MinIO object is deleted after the new one succeeds.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Photo stored"),
            @ApiResponse(responseCode = "400", description = "Empty/unsupported/oversized image"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller")
    })
    @PutMapping(value = "/{petId}/photo", consumes = "multipart/form-data")
    public ResponseEntity<HttpStatus> uploadPhoto(@PathVariable UUID petId,
                                            @RequestPart("file") MultipartFile file,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        petPhotoService.replacePhoto(petId, principal.id(), file);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a short-lived presigned URL for the profile photo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "{url: presigned access url}"),
            @ApiResponse(responseCode = "404", description = "No photo or pet not owned by the caller")
    })
    @GetMapping("/{petId}/photo-url")
    public Map<String, String> photoUrl(@PathVariable UUID petId,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("url", petPhotoService.getPhotoAccessUrl(petId, principal.id()));
    }

    /** Streams the profile photo bytes; ownership enforced, storage details hidden. */
    @Operation(summary = "Download the profile photo", description = "Streams raw bytes with the stored content type; internal storage paths are never exposed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image bytes"),
            @ApiResponse(responseCode = "404", description = "PET_PHOTO_NOT_FOUND or pet not owned by the caller")
    })
    @GetMapping("/{petId}/photo")
    ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody> getPhoto(
            @PathVariable UUID petId,
            @AuthenticationPrincipal UserPrincipal principal) {
        PetPhotoService.PhotoContent photo = petPhotoService.getPhotoContent(petId, principal.id());
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType(photo.contentType()));
        headers.setContentLength(photo.sizeBytes());
        headers.setCacheControl("private, max-age=300");
        return new ResponseEntity<>(out -> {
            try (java.io.InputStream in = photo.content()) {
                in.transferTo(out);
            }
        }, headers, org.springframework.http.HttpStatus.OK);
    }

    @Operation(summary = "Delete the profile photo", description = "Removes the MinIO object and the DB row; idempotent 204 when no photo exists.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Photo removed (or was absent)"),
            @ApiResponse(responseCode = "404", description = "Pet not found or not owned by the caller")
    })
    @DeleteMapping("/{petId}/photo")
    public ResponseEntity<HttpStatus> deletePhoto(@PathVariable UUID petId,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        petPhotoService.deletePhoto(petId, principal.id());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List breeds for a species", description = "Breed catalog used by breedId validation (a breed must match the pet's species).")
    @ApiResponse(responseCode = "200", description = "Breeds ordered by name")
    @GetMapping("/breeds")
    public List<BreedResponse> breeds(@RequestParam PetSpecies species,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return petService.listBreeds(species).stream().map(BreedResponse::from).toList();
    }

    /**
     * Legacy PATCH extras: applies the newer profile-section fields that were
     * added to UpdatePetRequest without touching the core service signature.
     * Same PATCH semantics as the section endpoints (null = unchanged).
     */
    private void applyLegacyExtras(Pet pet, UpdatePetRequest request) {
        if (request.birthDateEstimated() != null) {
            pet.setBirthDateEstimated(request.birthDateEstimated());
        }
        if (request.heightCm() != null) {
            pet.setHeightCm(request.heightCm());
        }
        if (request.bodySize() != null) {
            pet.setBodySize(request.bodySize());
        }
        if (request.eyeColor() != null) {
            pet.setEyeColor(blankToNull(request.eyeColor()));
        }
        if (request.coatLength() != null) {
            pet.setCoatLength(request.coatLength());
        }
        if (request.physicalDescription() != null) {
            pet.setPhysicalDescription(blankToNull(request.physicalDescription()));
        }
        if (request.identificationTag() != null) {
            pet.setIdentificationTag(blankToNull(request.identificationTag()));
        }
        if (request.registrationNumber() != null) {
            pet.setRegistrationNumber(blankToNull(request.registrationNumber()));
        }
        if (request.cityLicenseNumber() != null) {
            pet.setCityLicenseNumber(blankToNull(request.cityLicenseNumber()));
        }
        if (request.activityLevel() != null) {
            pet.setActivityLevel(request.activityLevel());
        }
        if (request.temperament() != null) {
            pet.setTemperament(petProfileService.canonicalizeTemperamentForLegacy(request.temperament()));
        }
        if (request.socialBehavior() != null) {
            pet.setSocialBehavior(blankToNull(request.socialBehavior()));
        }
        if (request.goodWithChildren() != null) {
            pet.setGoodWithChildren(request.goodWithChildren());
        }
        if (request.goodWithOtherPets() != null) {
            pet.setGoodWithOtherPets(request.goodWithOtherPets());
        }
        if (request.behavioralNotes() != null) {
            pet.setBehavioralNotes(blankToNull(request.behavioralNotes()));
        }
        if (request.dietType() != null) {
            pet.setDietType(blankToNull(request.dietType()));
        }
        if (request.foodType() != null) {
            pet.setFoodType(request.foodType());
        }
        if (request.foodBrand() != null) {
            pet.setFoodBrand(blankToNull(request.foodBrand()));
        }
        if (request.dailyMealCount() != null) {
            pet.setDailyMealCount(request.dailyMealCount());
        }
        if (request.dailyFoodAmountGrams() != null) {
            pet.setDailyFoodAmountGrams(request.dailyFoodAmountGrams());
        }
        if (request.dietaryRestrictions() != null) {
            pet.setDietaryRestrictions(blankToNull(request.dietaryRestrictions()));
        }
        if (request.acquisitionDate() != null) {
            pet.setAcquisitionDate(request.acquisitionDate());
        }
        if (request.acquisitionMethod() != null) {
            pet.setAcquisitionMethod(request.acquisitionMethod());
        }
        if (request.originDescription() != null) {
            pet.setOriginDescription(blankToNull(request.originDescription()));
        }
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.isEmpty() ? null : stripped;
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
