package social.benji.benji_backend_api.usermanagement.service;

import social.benji.benji_backend_api.usermanagement.domain.exception.UserManagementErrors;

/**
 * Normalizes arbitrary user input into a canonical E.164 string so that
 * different representations of the same number map to one identity.
 * Deliberately country-agnostic: no Iranian (or any single-country) assumptions
 * are hard-coded in the core rules.
 */
public final class MobileNumberNormalizer {

    private static final int MIN_NATIONAL_DIGITS = 8;
    private static final int MAX_E164_DIGITS = 15;

    // Default country used only when the caller supplies a national-format
    // number without any country code; configurable per deployment.
    private final String defaultCountryCode;

    public MobileNumberNormalizer(String defaultCountryCode) {
        this.defaultCountryCode = normalizeCountry(defaultCountryCode);
    }

    public String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw UserManagementErrors.invalidMobileNumber();
        }
        boolean hadPlus = false;
        StringBuilder digits = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == '+') {
                if (!digits.isEmpty() || hadPlus) {
                    throw UserManagementErrors.invalidMobileNumber();
                }
                hadPlus = true;
            } else if (Character.isDigit(ch)) {
                digits.append(ch);
            } else if (!isIgnorableSeparator(ch)) {
                throw UserManagementErrors.invalidMobileNumber();
            }
        }

        String number = digits.toString();
        if (number.isEmpty()) {
            throw UserManagementErrors.invalidMobileNumber();
        }

        if (hadPlus) {
            return validate("+" + number);
        }
        if (number.startsWith("00")) {
            return validate("+" + number.substring(2));
        }
        if (number.startsWith("0")) {
            // National format with trunk prefix -> attach default country code.
            return validate("+" + defaultCountryCode + number.substring(1));
        }
        if (number.startsWith(defaultCountryCode)) {
            return validate("+" + number);
        }
        // Bare number not matching the default country: assume it already
        // carries some other country code.
        return validate("+" + number);
    }

    private String validate(String e164) {
        int digitCount = e164.length() - 1;
        if (digitCount > MAX_E164_DIGITS || digitCount - defaultCountryCode.length() < MIN_NATIONAL_DIGITS) {
            throw UserManagementErrors.invalidMobileNumber();
        }
        return e164;
    }

    private static boolean isIgnorableSeparator(char ch) {
        return ch == ' ' || ch == '-' || ch == '(' || ch == ')' || ch == '.';
    }

    private static String normalizeCountry(String countryCode) {
        if (countryCode == null || !countryCode.matches("\\+?\\d{1,3}")) {
            throw new IllegalStateException(
                    "benji.mobile.default-country-code must be 1-3 digits, optionally '+'-prefixed");
        }
        return countryCode.startsWith("+") ? countryCode.substring(1) : countryCode;
    }

    /** For logging contexts that must never carry full numbers. */
    public static String mask(String normalized) {
        if (normalized == null || normalized.length() < 7) {
            return "***";
        }
        return normalized.substring(0, Math.min(normalized.length(), 5)) + "****"
                + normalized.substring(normalized.length() - 2);
    }
}
