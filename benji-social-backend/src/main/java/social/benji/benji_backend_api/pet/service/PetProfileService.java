package social.benji.benji_backend_api.pet.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import social.benji.benji_backend_api.pet.domain.Breed;
import social.benji.benji_backend_api.pet.domain.Pet;
import social.benji.benji_backend_api.pet.domain.exception.PetErrors;
import social.benji.benji_backend_api.pet.repository.BreedRepository;
import social.benji.benji_backend_api.pet.web.dto.CreatePetRequest;
import social.benji.benji_backend_api.pet.web.dto.UpdateBasicInfoRequest;
import social.benji.benji_backend_api.pet.web.dto.UpdateBehaviorRequest;
import social.benji.benji_backend_api.pet.web.dto.UpdateIdentificationRequest;
import social.benji.benji_backend_api.pet.web.dto.UpdateNutritionRequest;
import social.benji.benji_backend_api.pet.web.dto.UpdatePhysicalRequest;

/**
 * Extended-profile application service: full creation payload plus the five
 * section-specific PATCH operations. Kept separate from {@link PetService} so
 * the existing core-identity service (and every caller of it) stays untouched.
 *
 * <p>PATCH convention (project-wide): null = leave unchanged; a blank string
 * clears an optional text value; explicit booleans (including false) are real
 * values; a non-null temperament list REPLACES the stored set and an empty
 * list clears it.</p>
 */
@Service
public class PetProfileService {

    private static final BigDecimal MAX_WEIGHT_KG = new BigDecimal("200");
    private static final BigDecimal MIN_HEIGHT = new BigDecimal("0.1");
    private static final BigDecimal MAX_HEIGHT = new BigDecimal("300");
    /** Letters, digits, spaces and hyphens only; keeps the CSV storage safe. */
    private static final String TEMPERAMENT_TAG_PATTERN = "[A-Za-z\\u0600-\\u06FF][A-Za-z0-9 \\u0600-\\u06FF-]*";

    private final PetService petService;
    private final BreedRepository breedRepository;

    public PetProfileService(PetService petService, BreedRepository breedRepository) {
        this.petService = petService;
        this.breedRepository = breedRepository;
    }

    /**
     * Creates a pet with the minimal required fields (name + species) applied
     * by the core service, then fills in any optional profile attributes that
     * were supplied. Progressive completion remains the intended usage.
     */
    @Transactional
    public Pet create(UUID ownerId, CreatePetRequest request) {
        Pet pet = petService.create(ownerId, request.name(), request.species(),
                request.breedId(), request.sex(), request.dateOfBirth(),
                request.color(), request.weightKg(), request.microchipNumber(),
                request.neutered(), request.coatColor(),
                request.city(), request.bio());

        if (request.birthDateEstimated() != null) {
            pet.setBirthDateEstimated(request.birthDateEstimated());
        }
        applyPhysical(pet, request.heightCm(), request.bodySize(), request.eyeColor(),
                request.coatLength(), request.physicalDescription());
        applyIdentification(pet, request.identificationTag(), request.registrationNumber(),
                request.cityLicenseNumber());
        applyBehavior(pet, request.activityLevel(), request.temperament(), request.socialBehavior(),
                request.goodWithChildren(), request.goodWithOtherPets(), request.behavioralNotes());
        applyNutrition(pet, request.dietType(), request.foodType(), request.foodBrand(),
                request.dailyMealCount(), request.dailyFoodAmountGrams(), request.dietaryRestrictions());
        applyOwnership(pet, request.acquisitionDate(), request.acquisitionMethod(),
                request.originDescription());
        return pet;
    }

    // ----- Section PATCH operations ---------------------------------------

    @Transactional
    public Pet updateBasicInfo(UUID petId, UUID ownerId, UpdateBasicInfoRequest req) {
        Pet pet = editablePet(petId, ownerId);
        if (req.name() != null) {
            pet.setName(requireNonBlankName(req.name()));
        }
        if (req.breedId() != null) {
            validateBreedMatchesSpecies(req.breedId(), pet.getSpecies());
            pet.setBreedId(req.breedId());
        }
        if (req.sex() != null) {
            pet.setSex(req.sex());
        }
        if (req.dateOfBirth() != null) {
            if (req.dateOfBirth().isAfter(LocalDate.now())) {
                throw PetErrors.invalidDateOfBirth();
            }
            pet.setDateOfBirth(req.dateOfBirth());
        }
        if (req.birthDateEstimated() != null) {
            pet.setBirthDateEstimated(req.birthDateEstimated());
        }
        if (req.coatColor() != null) {
            pet.setCoatColor(blankToNull(req.coatColor()));
        }
        if (req.neutered() != null) {
            pet.setNeutered(req.neutered());
        }
        if (req.color() != null) {
            pet.setColor(blankToNull(req.color()));
        }
        if (req.city() != null) {
            pet.setCity(blankToNull(req.city()));
        }
        if (req.bio() != null) {
            pet.setBio(blankToNull(req.bio()));
        }
        return pet;
    }

    @Transactional
    public Pet updatePhysical(UUID petId, UUID ownerId, UpdatePhysicalRequest req) {
        Pet pet = editablePet(petId, ownerId);
        if (req.heightCm() != null) {
            BigDecimal h = req.heightCm();
            if (h.compareTo(MIN_HEIGHT) < 0 || h.compareTo(MAX_HEIGHT) > 0) {
                throw PetErrors.invalidHeight();
            }
            pet.setHeightCm(h);
        }
        applyPhysical(pet, req.currentWeightKg(), req.bodySize(), req.eyeColor(),
                req.coatLength(), req.physicalDescription());
        return pet;
    }

    @Transactional
    public Pet updateIdentification(UUID petId, UUID ownerId, UpdateIdentificationRequest req) {
        Pet pet = editablePet(petId, ownerId);
        if (req.microchipNumber() != null) {
            String normalized = normalizeMicrochip(req.microchipNumber());
            if (normalized != null && !normalized.matches("[A-Za-z0-9]{10,15}")) {
                throw PetErrors.invalidMicrochipNumber();
            }
            pet.setMicrochipNumber(normalized);
        }
        applyIdentification(pet, req.identificationTag(), req.registrationNumber(),
                req.cityLicenseNumber());
        return pet;
    }

    @Transactional
    public Pet updateBehavior(UUID petId, UUID ownerId, UpdateBehaviorRequest req) {
        Pet pet = editablePet(petId, ownerId);
        applyBehavior(pet, req.activityLevel(), req.temperament(), req.socialBehavior(),
                req.goodWithChildren(), req.goodWithOtherPets(), req.behavioralNotes());
        return pet;
    }

    @Transactional
    public Pet updateNutrition(UUID petId, UUID ownerId, UpdateNutritionRequest req) {
        Pet pet = editablePet(petId, ownerId);
        applyNutrition(pet, req.dietType(), req.foodType(), req.foodBrand(),
                req.dailyMealCount(), req.dailyFoodAmountGrams(), req.dietaryRestrictions());
        return pet;
    }

    // ----- Lifecycle operations -------------------------------------------

    @Transactional
    public void markLost(UUID petId, UUID ownerId) {
        transition(petId, ownerId, Pet::markLost);
    }

    @Transactional
    public void markFound(UUID petId, UUID ownerId) {
        transition(petId, ownerId, Pet::markFound);
    }

    @Transactional
    public void markRecovered(UUID petId, UUID ownerId) {
        transition(petId, ownerId, Pet::markRecovered);
    }

    @Transactional
    public void markRehomed(UUID petId, UUID ownerId) {
        transition(petId, ownerId, Pet::markRehomed);
    }

    // ----- Shared appliers (used by create and section PATCH) -------------

    private void applyPhysical(Pet pet, BigDecimal weightKg, Object bodySizeRaw, String eyeColor,
                               Object coatLengthRaw, String physicalDescription) {
        if (weightKg != null) {
            BigDecimal w = (BigDecimal) weightKg;
            if (w.signum() <= 0 || w.compareTo(MAX_WEIGHT_KG) >= 0) {
                throw PetErrors.invalidWeight();
            }
            pet.setCurrentWeightKg(w);
        }
        if (bodySizeRaw != null) {
            pet.setBodySize((social.benji.benji_backend_api.pet.domain.BodySize) bodySizeRaw);
        }
        if (eyeColor != null) {
            pet.setEyeColor(blankToNull(eyeColor));
        }
        if (coatLengthRaw != null) {
            pet.setCoatLength((social.benji.benji_backend_api.pet.domain.CoatLength) coatLengthRaw);
        }
        if (physicalDescription != null) {
            pet.setPhysicalDescription(blankToNull(physicalDescription));
        }
    }

    private void applyIdentification(Pet pet, String identificationTag, String registrationNumber,
                                     String cityLicenseNumber) {
        if (identificationTag != null) {
            pet.setIdentificationTag(blankToNull(identificationTag));
        }
        if (registrationNumber != null) {
            pet.setRegistrationNumber(blankToNull(registrationNumber));
        }
        if (cityLicenseNumber != null) {
            pet.setCityLicenseNumber(blankToNull(cityLicenseNumber));
        }
    }

    private void applyBehavior(Pet pet, Object activityLevelRaw, List<String> temperament,
                               String socialBehavior, Boolean goodWithChildren,
                               Boolean goodWithOtherPets, String behavioralNotes) {
        if (activityLevelRaw != null) {
            pet.setActivityLevel((social.benji.benji_backend_api.pet.domain.ActivityLevel) activityLevelRaw);
        }
        if (temperament != null) {
            pet.setTemperament(canonicalizeTemperament(temperament));
        }
        if (socialBehavior != null) {
            pet.setSocialBehavior(blankToNull(socialBehavior));
        }
        if (goodWithChildren != null) {
            pet.setGoodWithChildren(goodWithChildren);
        }
        if (goodWithOtherPets != null) {
            pet.setGoodWithOtherPets(goodWithOtherPets);
        }
        if (behavioralNotes != null) {
            pet.setBehavioralNotes(blankToNull(behavioralNotes));
        }
    }

    private void applyNutrition(Pet pet, String dietType, Object foodTypeRaw, String foodBrand,
                                Integer dailyMealCount, Integer dailyFoodAmountGrams,
                                String dietaryRestrictions) {
        if (dietType != null) {
            pet.setDietType(blankToNull(dietType));
        }
        if (foodTypeRaw != null) {
            pet.setFoodType((social.benji.benji_backend_api.pet.domain.FoodType) foodTypeRaw);
        }
        if (foodBrand != null) {
            pet.setFoodBrand(blankToNull(foodBrand));
        }
        if (dailyMealCount != null) {
            if (dailyMealCount <= 0) {
                throw PetErrors.invalidMealCount();
            }
            pet.setDailyMealCount(dailyMealCount);
        }
        if (dailyFoodAmountGrams != null) {
            if (dailyFoodAmountGrams <= 0) {
                throw PetErrors.invalidFoodAmount();
            }
            pet.setDailyFoodAmountGrams(dailyFoodAmountGrams);
        }
        if (dietaryRestrictions != null) {
            pet.setDietaryRestrictions(blankToNull(dietaryRestrictions));
        }
    }

    private void applyOwnership(Pet pet, LocalDate acquisitionDate, Object acquisitionMethodRaw,
                                String originDescription) {
        if (acquisitionDate != null) {
            if (acquisitionDate.isAfter(LocalDate.now())) {
                throw PetErrors.invalidAcquisitionDate();
            }
            pet.setAcquisitionDate(acquisitionDate);
        }
        if (acquisitionMethodRaw != null) {
            pet.setAcquisitionMethod(
                    (social.benji.benji_backend_api.pet.domain.AcquisitionMethod) acquisitionMethodRaw);
        }
        if (originDescription != null) {
            pet.setOriginDescription(blankToNull(originDescription));
        }
    }

    // ----- Helpers ---------------------------------------------------------

    /** Loads the owned pet and refuses edits on terminal (DECEASED/REHOMED) profiles. */
    private Pet editablePet(UUID petId, UUID ownerId) {
        Pet pet = petService.getOwnedPet(petId, ownerId);
        try {
            pet.ensureEditable();
        } catch (IllegalStateException e) {
            throw PetErrors.invalidStatusTransition();
        }
        return pet;
    }

    private void transition(UUID petId, UUID ownerId, java.util.function.Consumer<Pet> action) {
        Pet pet = petService.getOwnedPet(petId, ownerId);
        try {
            action.accept(pet);
        } catch (IllegalStateException e) {
            throw PetErrors.invalidStatusTransition();
        }
    }

    private String requireNonBlankName(String raw) {
        String normalized = raw.strip().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            throw PetErrors.invalidName();
        }
        return normalized;
    }

    private void validateBreedMatchesSpecies(UUID breedId, social.benji.benji_backend_api.pet.domain.PetSpecies species) {
        Breed breed = breedRepository.findById(breedId).orElseThrow(PetErrors::invalidBreed);
        if (breed.getSpecies() != species) {
            throw PetErrors.invalidBreed();
        }
    }

    /** Trim, dedupe, validate charset, join canonically. Public for the legacy PATCH path. */
    public String canonicalizeTemperamentForLegacy(List<String> tags) {
        return canonicalizeTemperament(tags);
    }

    /** Trim, drop empties, dedupe, validate charset, join canonically. */
    private String canonicalizeTemperament(List<String> tags) {
        if (tags.isEmpty()) {
            return null;
        }
        java.util.LinkedHashSet<String> unique = new java.util.LinkedHashSet<>();
        for (String raw : tags) {
            if (raw == null) {
                continue;
            }
            String tag = raw.strip();
            if (tag.isEmpty()) {
                continue;
            }
            if (!tag.matches(TEMPERAMENT_TAG_PATTERN)) {
                throw PetErrors.invalidTemperamentTag(tag);
            }
            unique.add(tag);
        }
        String joined = String.join(",", unique);
        if (joined.length() > 500) {
            throw PetErrors.invalidTemperamentTag("total length exceeds 500 characters");
        }
        return joined.isEmpty() ? null : joined;
    }

    private static String normalizeMicrochip(String raw) {
        String stripped = blankToNull(raw);
        return stripped == null ? null : stripped.replaceAll("[\\s-]", "");
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.isEmpty() ? null : stripped;
    }
}
