package social.benji.benji_backend_api.lostfound.domain;

import org.junit.jupiter.api.Test;
import social.benji.benji_backend_api.lostfound.domain.valueobject.Breed;

import static org.junit.jupiter.api.Assertions.*;

class BreedTest {
    @Test
    void supportsOptionalUnknownAndMixedBreeds() {
        assertEquals(Breed.UNKNOWN, Breed.of(null).value());
        assertEquals(Breed.UNKNOWN, Breed.of("").value());
        assertEquals(Breed.MIXED, Breed.of("mixed").value());
        assertEquals("PERSIAN", Breed.of(" persian ").value());
    }

    @Test
    void rejectsOverlongBreed() {
        assertThrows(IllegalArgumentException.class,
                () -> Breed.of("x".repeat(101)));
    }
}
