package prenotazione.medica.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Risposta strutturata per errori di validazione Bean Validation (400).
 * Restituita dal {@link GlobalExceptionHandler} quando viene lanciata
 * {@link org.springframework.web.bind.MethodArgumentNotValidException}.
 *
 * @param timestamp istante dell'errore
 * @param status    codice HTTP (400)
 * @param error     reason phrase (Bad Request)
 * @param message   messaggio generale (i18n)
 * @param path      path della richiesta
 * @param errors    lista di errori per campo (campo + messaggio tradotto)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidationErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDto> errors
) {
    public ValidationErrorResponse(int status, String error, String message, String path, List<FieldErrorDto> errors) {
        this(Instant.now(), status, error, message, path, errors);
    }

    /**
     * Singolo errore su un campo (nome campo + messaggio).
     */
    public record FieldErrorDto(String field, String message) {}
}
