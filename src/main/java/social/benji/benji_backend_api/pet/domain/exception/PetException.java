package social.benji.benji_backend_api.pet.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain/application error for the Pet module. Codes are stable machine-readable
 * identifiers exposed in API error payloads; messages must be safe to expose.
 */
public class PetException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public PetException(String code, String message, HttpStatus status) {
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
