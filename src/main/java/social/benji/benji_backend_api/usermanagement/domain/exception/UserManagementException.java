package social.benji.benji_backend_api.usermanagement.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain/application error carrying the API error code and HTTP status.
 * Messages must remain safe to expose (no enumeration hints, no secrets).
 */
public class UserManagementException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    /** Optional Retry-After hint in seconds (used by rate-limit errors). */
    private final long retryAfterSeconds;

    public UserManagementException(String code, String message, HttpStatus status) {
        this(code, message, status, 0);
    }

    public UserManagementException(String code, String message, HttpStatus status, long retryAfterSeconds) {
        super(message);
        this.code = code;
        this.status = status;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    /** @return positive seconds when the caller should retry later, otherwise 0. */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
