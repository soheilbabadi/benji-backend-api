package social.benji.benji_backend_api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;
import java.util.List;

@Slf4j
@Component
public class BenjiCustomException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2937969386484720825L;

    private static void logException(RuntimeException exception, String message) {
        String className = exception.getClass().getSimpleName();
        log.error("{}: {}", className, message, exception);
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public static class ResourceNotFoundException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 8490379462774046464L;

        public ResourceNotFoundException(String message) {
            super(message);
            logException(this, message);
        }
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public static class ResourceUnauthorizedException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 8490379462774046464L;

        public ResourceUnauthorizedException(String message) {
            super(message);
            logException(this, message);
        }
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public static class AccessDeniedException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 8824943035015754069L;

        public AccessDeniedException(String message) {
            super(message);
            logException(this, message);
        }
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public static class ResourceForbiddenException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 8824943035015754070L;

        public ResourceForbiddenException(String message) {
            super(message);
            logException(this, message);
        }
    }

    @ResponseStatus(value = HttpStatus.CONFLICT)
    public static class DuplicateEntityException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 6097697152768645228L;

        public DuplicateEntityException(String message) {
            super(message);
            logException(this, message);
        }
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public static class ApplicationServerException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = -1584388453062884064L;

        public ApplicationServerException(String message) {
            super(message);
            logException(this, message);
        }
    }

    public static class ExcelException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = -9214904749662505366L;

        public ExcelException(String message) {
            super(message);
            logException(this, message);
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public static class ParentNotFoundException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = -5814948709735338639L;

        public ParentNotFoundException(String message) {
            super(message);
            logException(this, message);
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public static class ValidationException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = -5814948709735338639L;

        public ValidationException(String message) {
            super(message);
            logException(this, message);
        }

        public ValidationException(String message, List<String> errors) {
            super(message + ": " + String.join(", ", errors));
            logException(this, message);
        }
    }

    public static class FileContentException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 8038292358982007090L;

        public FileContentException(String message) {
            super(message);
            logException(this, message);
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_GATEWAY)
    public static class BpmsClientException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = -361685711987710436L;

        public BpmsClientException(String message, List<String> errors) {
            super(message + ": " + String.join(", ", errors));
            logException(this, message);
        }
    }

    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
    public static class PricingCalculationException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 4798201507748740416L;

        public PricingCalculationException(String message, List<String> errors) {
            super(message + ": " + String.join(", ", errors));
            logException(this, message);
        }
    }
}
