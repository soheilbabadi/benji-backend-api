package social.benji.benji_backend_api.petmedia.controller;

import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import social.benji.benji_backend_api.petmedia.application.exception.MediaException;

/**
 * Maps pet-media domain errors (MediaException) to their declared HTTP status
 * with a stable machine-readable code in the body. Scoped to this module's
 * controllers so it never shadows the global BenjiExceptionHandler.
 */
@RestControllerAdvice(assignableTypes = {PetAlbumController.class, PetPhotoController.class})
@Order(0)
public class PetMediaExceptionHandlerAdvice {

    @ExceptionHandler(MediaException.class)
    public ResponseEntity<Map<String, String>> handleMediaException(MediaException e) {
        return ResponseEntity.status(e.getStatus())
                .body(Map.of("code", e.getCode(), "message", e.getMessage()));
    }
}
