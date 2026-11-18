package social.benji.benji_backend_api.lostfound.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateAdvertisementRequestValidationTest {
    private static Validator validator;
    private static jakarta.validation.ValidatorFactory factory;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validLostRequest() {
        CreateLostAdvertisementRequest request = new CreateLostAdvertisementRequest(
                null, "DOG", null, null, "Brown dog",
                null, LocalDate.now().minusDays(1),
                "Iran", "Tehran", null, null, null);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validFoundRequest() {
        CreateFoundAdvertisementRequest request = new CreateFoundAdvertisementRequest(
                null, "CAT", "MIXED", "Gray cat",
                null, LocalDate.now().minusDays(1),
                "Iran", "Tehran", null, null, null);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsMissingRequiredFieldsAndBlankStrings() {
        CreateLostAdvertisementRequest request = new CreateLostAdvertisementRequest(
                null, "", null, null, " ",
                null, null, "", "", null, null, null);

        Set<ConstraintViolation<CreateLostAdvertisementRequest>> violations =
                validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsInvalidSpeciesAndOverlongDescription() {
        CreateFoundAdvertisementRequest request = new CreateFoundAdvertisementRequest(
                null, "BIRD", null, "x".repeat(2001),
                null, LocalDate.now(),
                "Iran", "Tehran", null, null, null);

        Set<ConstraintViolation<CreateFoundAdvertisementRequest>> violations =
                validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void optionalFieldsRemainOptional() {
        CreateFoundAdvertisementRequest request = new CreateFoundAdvertisementRequest(
                null, "CAT", null, "A found cat",
                null, LocalDate.now(),
                "Iran", "Tehran", null, null, null);

        assertTrue(validator.validate(request).isEmpty());
    }
}
