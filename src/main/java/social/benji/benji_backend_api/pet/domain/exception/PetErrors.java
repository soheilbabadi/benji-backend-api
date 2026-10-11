package social.benji.benji_backend_api.pet.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Factory for the Pet module's error vocabulary. Ownership violations are
 * reported as PET_NOT_FOUND so callers cannot enumerate existing pet ids.
 */
public final class PetErrors {

    private PetErrors() {
    }

    public static PetException petNotFound() {
        return new PetException(
                "PET_NOT_FOUND",
                "Pet not found.",
                HttpStatus.NOT_FOUND);
    }

    public static PetException invalidName() {
        return new PetException(
                "INVALID_PET_NAME",
                "The pet name is not valid.",
                HttpStatus.BAD_REQUEST);
    }

    public static PetException invalidBreed() {
        return new PetException(
                "INVALID_BREED",
                "The selected breed does not match the pet species.",
                HttpStatus.BAD_REQUEST);
    }

    public static PetException invalidDateOfBirth() {
        return new PetException(
                "INVALID_DATE_OF_BIRTH",
                "The date of birth must not be in the future.",
                HttpStatus.BAD_REQUEST);
    }

    public static PetException invalidWeight() {
        return new PetException(
                "INVALID_WEIGHT",
                "The weight must be between 0 and 200 kg.",
                HttpStatus.BAD_REQUEST);
    }

    public static PetException invalidMicrochipNumber() {
        return new PetException(
                "INVALID_MICROCHIP_NUMBER",
                "The microchip number format is not valid.",
                HttpStatus.BAD_REQUEST);
    }

    public static PetException invalidStatusTransition() {
        return new PetException(
                "INVALID_PET_STATUS_TRANSITION",
                "This operation is not allowed for the pet's current status.",
                HttpStatus.CONFLICT);
    }

    public static PetException photoNotFound() {
        return new PetException(
                "PET_PHOTO_NOT_FOUND",
                "Pet photo not found.",
                HttpStatus.NOT_FOUND);
    }

    public static PetException unsupportedPhotoContentType(String contentType) {
        return new PetException(
                "UNSUPPORTED_PHOTO_CONTENT_TYPE",
                "Unsupported image type: " + contentType,
                HttpStatus.BAD_REQUEST);
    }

    public static PetException photoTooLarge(long maxBytes) {
        return new PetException(
                "PHOTO_TOO_LARGE",
                "The photo exceeds the maximum allowed size of " + (maxBytes / (1024 * 1024)) + " MB.",
                HttpStatus.BAD_REQUEST);
    }

    public static PetException emptyPhoto() {
        return new PetException(
                "EMPTY_PHOTO",
                "The uploaded photo is empty.",
                HttpStatus.BAD_REQUEST);
    }
}
