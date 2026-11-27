package social.benji.benji_backend_api.lostfound.domain.valueobject;

import java.util.Objects;

public record Location(
        String country,
        String city,
        String area,
        String description
) {
    public Location {
        country = requireText(country, "Country", 100);
        city = requireText(city, "City", 100);
        area = area == null ? null : requireOptionalText(area, "Area", 100);
        description = description == null ? null : requireOptionalText(description, "Location description", 500);
    }

    private static String requireText(String value, String name, int maximum) {
        String normalized = requireOptionalText(value, name, maximum);
        if (normalized == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        return normalized;
    }

    private static String requireOptionalText(String value, String name, int maximum) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maximum) {
            throw new IllegalArgumentException(name + " must not exceed " + maximum + " characters");
        }
        return normalized;
    }
}
