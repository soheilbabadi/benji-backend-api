package social.benji.benji_backend_api.pet.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import social.benji.benji_backend_api.pet.valueobject.FoodType;

/** Section 5 of the profile. Null fields stay unchanged; blanks clear values. */
public record UpdateNutritionRequest(
        @Size(max = 100) String dietType,
        FoodType foodType,
        @Size(max = 100) String foodBrand,
        @Min(1) @Max(12) Integer dailyMealCount,
        @Min(1) @Max(100000) Integer dailyFoodAmountGrams,
        @Size(max = 500) String dietaryRestrictions) {
}
