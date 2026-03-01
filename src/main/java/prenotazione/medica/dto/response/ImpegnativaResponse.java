package prenotazione.medica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Risposta generica dopo emissione/gestione impegnativa (esito e messaggio).
 * <p>
 * Usato dalle API di creazione o aggiornamento impegnativa per restituire successo/errore senza
 * esporre l'intera entità.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImpegnativaResponse
{
    private Boolean isSuccess;
    private String message;
}