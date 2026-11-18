package social.benji.benji_backend_api.lostfound.domain;

import org.junit.jupiter.api.Test;
import social.benji.benji_backend_api.lostfound.domain.valueobject.PetSpecies;

import static org.junit.jupiter.api.Assertions.*;

class PetSpeciesTest {
    @Test
    void acceptsDogAndCatCaseInsensitively() {
        assertEquals(PetSpecies.DOG, PetSpecies.from("dog"));
        assertEquals(PetSpecies.CAT, PetSpecies.from(" CAT "));
    }

    @Test
    void rejectsOtherSpecies() {
        assertThrows(IllegalArgumentException.class, () -> PetSpecies.from("BIRD"));
        assertThrows(IllegalArgumentException.class, () -> PetSpecies.from(" "));
    }
}
