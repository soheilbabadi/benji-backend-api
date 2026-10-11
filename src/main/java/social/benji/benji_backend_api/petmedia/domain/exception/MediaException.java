package social.benji.benji_backend_api.petmedia.domain.exception;

import org.springframework.http.HttpStatus;

/** Domain/application error carrying a stable machine-readable code. */
public class MediaException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public MediaException(String code, String message, HttpStatus status) {
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
