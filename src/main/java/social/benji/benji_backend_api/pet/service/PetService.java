package social.benji.benji_backend_api.pet.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import social.benji.benji_backend_api.pet.domain.Breed;
import social.benji.benji_backend_api.pet.domain.Pet;
import social.benji.benji_backend_api.pet.domain.PetSex;
import social.benji.benji_backend_api.pet.domain.PetSpecies;
import social.benji.benji_backend_api.pet.domain.exception.PetErrors;
import social.benji.benji_backend_api.pet.repository.BreedRepository;
import social.benji.benji_backend_api.pet.repository.PetRepository;

/**
 * Application service for pet identity management. Ownership is enforced here,
 * in the business layer — never trusted from controller-level checks alone. A
 * pet owned by someone else is reported as not found to prevent id enumeration.
 */
@Service
public class PetService {

    private final PetRepository petRepository;
    private final BreedRepository breedRepository;

    public PetService(PetRepository petRepository, BreedRepository breedRepository) {
        this.petRepository = petRepository;
        this.breedRepository = breedRepository;
    }

    @Transactional
    public Pet create(UUID ownerId, String name, PetSpecies species, UUID breedId, PetSex sex,
                      java.time.LocalDate dateOfBirth, String color,
                      java.math.BigDecimal weightKg, String microchipNumber) {
        validateFieldRules(name, dateOfBirth, weightKg, microchipNumber);
        validateBreedMatchesSpecies(breedId, species);

        Pet pet = Pet.create(ownerId, normalizeName(name), species);
        pet.setBreedId(breedId);
        if (sex != null) {
            pet.setSex(sex);
        }
        pet.setDateOfBirth(dateOfBirth);
        pet.setColor(blankToNull(color));
        pet.setCurrentWeightKg(weightKg);
        pet.setMicrochipNumber(normalizeMicrochip(microchipNumber));
        return petRepository.save(pet);
    }

    @Transactional(readOnly = true)
    public List<Pet> listMyPets(UUID ownerId, boolean includeArchived) {
        if (includeArchived) {
            return petRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
        }
        return petRepository.findByOwnerIdAndStatusOrderByCreatedAtDesc(
                ownerId, social.benji.benji_backend_api.pet.domain.PetStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Pet getOwnedPet(UUID petId, UUID ownerId) {
        return petRepository.findByIdAndOwnerId(petId, ownerId)
                .orElseThrow(PetErrors::petNotFound);
    }

    @Transactional
    public Pet update(UUID petId, UUID ownerId, String name, UUID breedId, PetSex sex,
                      java.time.LocalDate dateOfBirth, String color,
                      java.math.BigDecimal weightKg, String microchipNumber) {
        Pet pet = getOwnedPet(petId, ownerId);
        try {
            pet.ensureEditable();
        } catch (IllegalStateException e) {
            throw PetErrors.invalidStatusTransition();
        }
        if (name != null) {
            validateFieldRules(name, null, null, null);
            pet.setName(normalizeName(name));
        }
        if (breedId != null) {
            validateBreedMatchesSpecies(breedId, pet.getSpecies());
            pet.setBreedId(breedId);
        }
        if (sex != null) {
            pet.setSex(sex);
        }
        if (dateOfBirth != null) {
            validateFieldRules(null, dateOfBirth, null, null);
            pet.setDateOfBirth(dateOfBirth);
        }
        if (color != null) {
            pet.setColor(blankToNull(color));
        }
        if (weightKg != null) {
            validateFieldRules(null, null, weightKg, null);
            pet.setCurrentWeightKg(weightKg);
        }
        if (microchipNumber != null) {
            validateFieldRules(null, null, null, microchipNumber);
            pet.setMicrochipNumber(normalizeMicrochip(microchipNumber));
        }
        return pet;
    }

    @Transactional
    public void archive(UUID petId, UUID ownerId) {
        transition(getOwnedPet(petId, ownerId), Pet::archive);
    }

    @Transactional
    public void restore(UUID petId, UUID ownerId) {
        transition(getOwnedPet(petId, ownerId), Pet::restore);
    }

    @Transactional
    public void markDeceased(UUID petId, UUID ownerId) {
        transition(getOwnedPet(petId, ownerId), Pet::markDeceased);
    }

    @Transactional(readOnly = true)
    public List<Breed> listBreeds(PetSpecies species) {
        return breedRepository.findBySpeciesOrderByNameAsc(species);
    }

    /** Shared ownership gate for the photo service. */
    @Transactional(readOnly = true)
    public Pet requireOwnedPet(UUID petId, UUID ownerId) {
        return getOwnedPet(petId, ownerId);
    }

    private void transition(Pet pet, java.util.function.Consumer<Pet> action) {
        try {
            action.accept(pet);
        } catch (IllegalStateException e) {
            throw PetErrors.invalidStatusTransition();
        }
    }

    private void validateFieldRules(String name, java.time.LocalDate dob,
                                    java.math.BigDecimal weight, String microchip) {
        if (name != null && normalizeName(name).isEmpty()) {
            throw PetErrors.invalidName();
        }
        if (dob != null && dob.isAfter(java.time.LocalDate.now())) {
            throw PetErrors.invalidDateOfBirth();
        }
        if (weight != null
                && (weight.signum() <= 0 || weight.compareTo(new java.math.BigDecimal("200")) >= 0)) {
            throw PetErrors.invalidWeight();
        }
        if (microchip != null && !blankToNull(microchip).isEmpty()
                && !normalizeMicrochip(microchip).matches("[A-Za-z0-9]{10,15}")) {
            throw PetErrors.invalidMicrochipNumber();
        }
    }

    private void validateBreedMatchesSpecies(UUID breedId, PetSpecies species) {
        if (breedId == null) {
            return;
        }
        Breed breed = breedRepository.findById(breedId)
                .orElseThrow(PetErrors::invalidBreed);
        if (breed.getSpecies() != species) {
            throw PetErrors.invalidBreed();
        }
    }

    /** Collapse internal whitespace runs; Unicode names are fully supported. */
    private static String normalizeName(String raw) {
        return raw == null ? "" : raw.strip().replaceAll("\\s+", " ");
    }

    /** Microchip ids are digits or alphanumerics; strip spaces/dashes only. */
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
