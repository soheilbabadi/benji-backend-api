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

    /** Uniform failure used when no active account matches an authenticated principal. */
    public static Supplier<UserManagementException> accountUnavailable() {
        return () -> new UserManagementException(
                "AUTHENTICATION_FAILED",
                "Authentication failed.",
                HttpStatus.UNAUTHORIZED);
    }
}
