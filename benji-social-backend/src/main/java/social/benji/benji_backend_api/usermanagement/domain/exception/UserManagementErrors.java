package social.benji.benji_backend_api.usermanagement.domain.exception;

import java.util.function.Supplier;

import org.springframework.http.HttpStatus;

/**
 * Factory for the module's error vocabulary. Codes are stable machine-readable
 * identifiers exposed in API error payloads.
 */
public final class UserManagementErrors {

    private UserManagementErrors() {
    }

    public static UserManagementException invalidMobileNumber() {
        return new UserManagementException(
                "INVALID_MOBILE_NUMBER",
                "The mobile number is not valid.",
                HttpStatus.BAD_REQUEST);
    }

    public static UserManagementException otpInvalid() {
        return new UserManagementException(
                "OTP_INVALID",
                "The verification code is incorrect.",
                HttpStatus.UNAUTHORIZED);
    }

    public static UserManagementException otpExpired() {
        return new UserManagementException(
                "OTP_EXPIRED",
                "The verification code has expired.",
                HttpStatus.UNAUTHORIZED);
    }

    public static UserManagementException otpTooManyAttempts() {
        return new UserManagementException(
                "OTP_TOO_MANY_ATTEMPTS",
                "Too many incorrect attempts. Please request a new code.",
                HttpStatus.UNAUTHORIZED);
    }

    public static UserManagementException otpRateLimited() {
        return new UserManagementException(
                "OTP_RATE_LIMITED",
                "Please wait before requesting another code.",
                HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Rate-limit rejection that also carries the Retry-After hint (seconds) so
     * clients know exactly when the next OTP request will be accepted.
     */
    public static UserManagementException otpRateLimited(long retryAfterSeconds) {
        return new UserManagementException(
                "OTP_RATE_LIMITED",
                "Please wait at least " + retryAfterSeconds + " seconds before requesting another code.",
                HttpStatus.TOO_MANY_REQUESTS,
                retryAfterSeconds);
    }

    public static UserManagementException userBlocked() {
        return new UserManagementException(
                "USER_BLOCKED",
                "This account is not allowed to sign in.",
                HttpStatus.FORBIDDEN);
    }

    public static UserManagementException authenticationFailed() {
        return new UserManagementException(
                "AUTHENTICATION_FAILED",
                "Authentication failed.",
                HttpStatus.UNAUTHORIZED);
    }

    public static UserManagementException invalidRefreshToken() {
        return new UserManagementException(
                "INVALID_REFRESH_TOKEN",
                "The refresh token is invalid.",
                HttpStatus.UNAUTHORIZED);
    }

    public static UserManagementException emailAlreadyInUse() {
        return new UserManagementException(
                "EMAIL_ALREADY_IN_USE",
                "This email address is already in use.",
                HttpStatus.CONFLICT);
    }

    public static UserManagementException invalidUsername() {
        return new UserManagementException(
                "INVALID_USERNAME",
                "Username must be 3-30 characters using letters, digits, dots or underscores.",
                HttpStatus.BAD_REQUEST);
    }

    public static UserManagementException nameRequired() {
        return new UserManagementException(
                "NAME_REQUIRED",
                "Name must not be blank.",
                HttpStatus.BAD_REQUEST);
    }

    public static UserManagementException usernameAlreadyInUse() {
        return new UserManagementException(
                "USERNAME_ALREADY_IN_USE",
                "This username is already taken.",
                HttpStatus.CONFLICT);
    }

    public static UserManagementException invalidAvatar() {
        return new UserManagementException(
                "INVALID_AVATAR",
                "Avatar must be a JPEG, PNG or WebP image matching the declared content type.",
                HttpStatus.BAD_REQUEST);
    }

    public static UserManagementException avatarTooLarge() {
        return new UserManagementException(
                "AVATAR_TOO_LARGE",
                "Avatar must be non-empty and at most 5 MB.",
                HttpStatus.BAD_REQUEST);
    }

    public static UserManagementException avatarStorageUnavailable() {
        return new UserManagementException(
                "AVATAR_STORAGE_UNAVAILABLE",
                "The image storage service is currently unavailable.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    public static UserManagementException invalidRole(String role) {
        return new UserManagementException(
                "INVALID_ROLE",
                "Unknown role \"" + (role == null ? "" : role)
                        + "\". Allowed roles: USER, EXPERT, ADMIN.",
                HttpStatus.BAD_REQUEST);
    }

    /** Uniform message for any missing target; never leaks whether the id exists at all. */
    public static UserManagementException userNotFound() {
        return new UserManagementException(
                "USER_NOT_FOUND",
                "No active user was found for the given identifier.",
                HttpStatus.NOT_FOUND);
    }

    public static UserManagementException cannotChangeOwnRole() {
        return new UserManagementException(
                "CANNOT_CHANGE_OWN_ROLE",
                "You cannot change your own role.",
                HttpStatus.BAD_REQUEST);
    }

    public static UserManagementException profileNotFound() {
        return new UserManagementException(
                "PROFILE_NOT_FOUND",
                "No active profile was found for the given identifier.",
                HttpStatus.NOT_FOUND);
    }

    /** Uniform failure used when no active account matches an authenticated principal. */
    public static Supplier<UserManagementException> accountUnavailable() {
        return () -> new UserManagementException(
                "AUTHENTICATION_FAILED",
                "Authentication failed.",
                HttpStatus.UNAUTHORIZED);
    }
}
