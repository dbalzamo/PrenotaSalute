package prenotazione.medica.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import prenotazione.medica.shared.i18n.I18nMessageService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestore globale delle eccezioni: intercetta le eccezioni lanciate dai controller e dai service
 * (propagate fino al dispatcher) e restituisce una risposta JSON con messaggio tradotto (i18n).
 * <p>
 * Le eccezioni custom ({@link ResourceNotFoundException}, {@link BadRequestException},
 * {@link UnauthorizedException}) espongono una chiave i18n e argomenti; il messaggio finale
 * è risolto tramite {@link I18nMessageService}.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final I18nMessageService i18n;

    public GlobalExceptionHandler(I18nMessageService i18n) {
        this.i18n = i18n;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        String message = i18n.getMessage(ex.getMessageKey(), ex.getArgs());
        logger.warn("Resource not found: {} - {}", ex.getMessageKey(), message);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        message,
                        request.getRequestURI()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {
        String message = i18n.getMessage(ex.getMessageKey(), ex.getArgs());
        logger.warn("Bad request: {} - {}", ex.getMessageKey(), message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        message,
                        request.getRequestURI()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {
        String message = i18n.getMessage(ex.getMessageKey(), ex.getArgs());
        logger.warn("Unauthorized: {} - {}", ex.getMessageKey(), message);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        message,
                        request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String globalMessage = i18n.getMessage("error.badrequest");
        List<ValidationErrorResponse.FieldErrorDto> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationErrorResponse.FieldErrorDto(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());
        logger.warn("Validation failed on {}: {} errors", request.getRequestURI(), errors.size());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        globalMessage,
                        request.getRequestURI(),
                        errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        String message = ex.getMessage();
        logger.warn("Validation error: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        message,
                        request.getRequestURI()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request) {
        String message = ex.getMessage();
        logger.warn("Username not found: {}", message);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        message,
                        request.getRequestURI()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {
        String message = i18n.getMessage("error.generic");
        logger.error("Runtime error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        message,
                        request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        String message = i18n.getMessage("error.generic");
        logger.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        message,
                        request.getRequestURI()));
    }
}
