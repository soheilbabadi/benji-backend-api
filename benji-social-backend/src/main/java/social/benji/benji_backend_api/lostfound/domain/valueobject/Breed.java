package social.benji.benji_backend_api.lostfound.domain.valueobject;

import java.util.Locale;
import java.util.Objects;

public final class Breed {
    public static final String UNKNOWN = "UNKNOWN";
    public static final String MIXED = "MIXED";

    private final String value;

    private Breed(String value) {
        this.value = value;
    }

    public static Breed unknown() {
        return new Breed(UNKNOWN);
    }

    public static Breed mixed() {
        return new Breed(MIXED);
    }

    public static Breed of(String value) {
        if (value == null || value.isBlank()) {
            return unknown();
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (UNKNOWN.equals(normalized) || MIXED.equals(normalized)) {
            return new Breed(normalized);
        }
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Breed must not exceed 100 characters");
        }
        return new Breed(normalized);
    }

    public String value() {
        return value;
    }

    public boolean isUnknown() {
        return UNKNOWN.equals(value);
    }

    public boolean isMixed() {
        return MIXED.equals(value);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Breed breed && Objects.equals(value, breed.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
