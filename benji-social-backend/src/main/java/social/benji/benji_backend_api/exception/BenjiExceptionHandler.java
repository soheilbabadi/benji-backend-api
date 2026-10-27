package social.benji.benji_backend_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class BenjiExceptionHandler {

    private final MessageSource messageSource;

    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        log.warn("[{}] {} — {}", status.value(), request.getRequestURI(), message);
        return ResponseEntity.status(status).body(new ErrorResponse(status, message, request.getRequestURI()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        String localizedMessage = getMessage("exception.constraint_violation", message);
        return build(HttpStatus.valueOf(422), localizedMessage, request);
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ErrorResponse> handleTransactionException(
            TransactionException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.transaction_error", ex.getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, localizedMessage, request);
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ErrorResponse> handleJpaSystemException(
            JpaSystemException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.jpa_system_error", ex.getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, localizedMessage, request);
    }

    @ExceptionHandler(BenjiCustomException.ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            BenjiCustomException.ValidationException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.validation_error", ex.getMessage());
        return build(HttpStatus.NOT_ACCEPTABLE, localizedMessage, request);
    }

    @ExceptionHandler(BenjiCustomException.ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            BenjiCustomException.ResourceNotFoundException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.resource_not_found", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, localizedMessage, request);
    }

    @ExceptionHandler(BenjiCustomException.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            BenjiCustomException.AccessDeniedException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.access_denied", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, localizedMessage, request);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSpringAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.access_denied", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, localizedMessage, request);
    }

    @ExceptionHandler(BenjiCustomException.ResourceUnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleResourceUnauthorizedException(
            BenjiCustomException.ResourceUnauthorizedException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.resource_unauthorized", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, localizedMessage, request);
    }

    @ExceptionHandler(BenjiCustomException.FileContentException.class)
    public ResponseEntity<ErrorResponse> handleFileContentException(
            BenjiCustomException.FileContentException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.file_content_error", ex.getMessage());
        return build(HttpStatus.valueOf(422), localizedMessage, request);
    }

    @ExceptionHandler(BenjiCustomException.ParentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleParentNotFoundException(
            BenjiCustomException.ParentNotFoundException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.parent_not_found", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, localizedMessage, request);
    }

    @ExceptionHandler(BenjiCustomException.ExcelException.class)
    public ResponseEntity<ErrorResponse> handleExcelException(
            BenjiCustomException.ExcelException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.excel_error", ex.getMessage());
        return build(HttpStatus.NOT_ACCEPTABLE, localizedMessage, request);
    }

    @ExceptionHandler(BenjiCustomException.DuplicateEntityException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEntityException(
            BenjiCustomException.DuplicateEntityException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.duplicate_entity", ex.getMessage());
        return build(HttpStatus.CONFLICT, localizedMessage, request);
    }

    @ExceptionHandler(BenjiCustomException.ApplicationServerException.class)
    public ResponseEntity<ErrorResponse> handleApplicationServerException(
            BenjiCustomException.ApplicationServerException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.application_server_error", ex.getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, localizedMessage, request);
    }

    @ExceptionHandler(BenjiCustomException.BpmsClientException.class)
    public ResponseEntity<ErrorResponse> handleBpmsClientException(
            BenjiCustomException.BpmsClientException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.bpms_client_error", ex.getMessage());
        return build(HttpStatus.BAD_GATEWAY, localizedMessage, request);
    }

    @ExceptionHandler(BenjiCustomException.PricingCalculationException.class)
    public ResponseEntity<ErrorResponse> handlePricingCalculationException(
            BenjiCustomException.PricingCalculationException ex, HttpServletRequest request) {
        String localizedMessage = getMessage("exception.pricing_calculation_error", ex.getMessage());
        return build(HttpStatus.PRECONDITION_FAILED, localizedMessage, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        String localizedMessage = getMessage("error.global.internal_server_error");
        return build(HttpStatus.INTERNAL_SERVER_ERROR, localizedMessage, request);
    }

    @Getter
    public static class ErrorResponse {
        private final long timestamp = Instant.now().toEpochMilli();
        private final int status;
        private final String message;
        private final String path;

        public ErrorResponse(HttpStatus httpStatus, String message, String path) {
            this.status = httpStatus.value();
            this.message = message;
            this.path = path;
        }
    }
}
