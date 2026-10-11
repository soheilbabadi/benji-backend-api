package social.benji.benji_backend_api.pet.web;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import social.benji.benji_backend_api.pet.domain.exception.PetException;

/**
 * Module-local error advice: maps PetException to the shared API error shape
 * without leaking internal details. Scoped by assignableTypes so it never
 * interferes with other modules' exception handling.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = PetController.class)
class PetExceptionHandlerAdvice {

    record ApiError(String code, String message, Instant timestamp) {
        static ApiError of(PetException e) {
            return new ApiError(e.getCode(), e.getMessage(), Instant.now());
        }
    }

    @ExceptionHandler(PetException.class)
    ResponseEntity<ApiError> handlePetException(PetException e, HttpServletRequest request) {
        log.warn("Pet API failure [{}] {} — {}", e.getCode(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(ApiError.of(e));
    }
}
