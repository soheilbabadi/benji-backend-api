package social.benji.benji_backend_api.lostfound.domain.valueobject;

import java.util.Locale;

public enum PetSpecies {
    DOG,
    CAT;

    public static PetSpecies from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Species is required");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Species must be DOG or CAT");
        }
    }
}
