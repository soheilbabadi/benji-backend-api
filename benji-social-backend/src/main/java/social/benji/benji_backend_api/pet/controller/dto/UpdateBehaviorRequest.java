package social.benji.benji_backend_api.pet.controller.dto;

import java.util.List;

import jakarta.validation.constraints.Size;
import social.benji.benji_backend_api.pet.valueobject.ActivityLevel;

/**
 * Section 4 of the profile. Null fields stay unchanged; blanks clear values.
 * A non-null temperament list REPLACES the stored set (empty list clears it).
 */
public record UpdateBehaviorRequest(
        ActivityLevel activityLevel,
        @Size(max = 20) List<@Size(max = 50) String> temperament,
        @Size(max = 500) String socialBehavior,
        Boolean goodWithChildren,
        Boolean goodWithOtherPets,
        @Size(max = 1000) String behavioralNotes) {
}
