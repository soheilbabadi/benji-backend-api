package social.benji.benji_backend_api.usermanagement.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain/application error carrying the API error code and HTTP status.
 * Messages must remain safe to expose (no enumeration hints, no secrets).
 */
public class UserManagementException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public UserManagementException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
