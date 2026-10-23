package social.benji.benji_backend_api.usermanagement.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import social.benji.benji_backend_api.usermanagement.domain.exception.UserManagementException;

/**
 * Maps the user-management module's domain errors onto RFC 7807 problem
 * responses, exposing the stable machine-readable code as a property.
 */
@RestControllerAdvice(basePackages = "social.benji.benji_backend_api.usermanagement")
public class UserManagementExceptionHandlerAdvice {

    @ExceptionHandler(UserManagementException.class)
    public ResponseEntity<ProblemDetail> handleUserManagementException(UserManagementException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setTitle(HttpStatus.valueOf(ex.getStatus().value()).getReasonPhrase());
        problem.setProperty("code", ex.getCode());
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(ex.getStatus());
        if (ex.getRetryAfterSeconds() > 0) {
            // Standard rate-limit affordance so clients can back off precisely.
            builder.header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()));
            problem.setProperty("retryAfterSeconds", ex.getRetryAfterSeconds());
        }
        return builder.body(problem);
    }
}
