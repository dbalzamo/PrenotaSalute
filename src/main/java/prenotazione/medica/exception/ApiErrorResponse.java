package prenotazione.medica.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * DTO per la risposta di errore delle API.
 * Restituito dal {@link GlobalExceptionHandler} in formato JSON.
 *
 * @param timestamp istante dell'errore
 * @param status    codice HTTP (es. 404, 400)
 * @param error     reason phrase (es. Not Found, Bad Request)
 * @param message   messaggio leggibile (tradotto tramite i18n)
 * @param path      path della richiesta (opzionale)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public ApiErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path);
    }
}
