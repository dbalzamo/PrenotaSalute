package prenotazione.medica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.model.Account;

/**
 * Risposta dopo la registrazione (signup) di un nuovo utente.
 * <p>
 * Restituito da POST {@code /api/auth/signup}. Indica esito, messaggio e l'account creato (per
 * eventuale uso lato client dopo il login automatico).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse
{
    private Boolean isSuccess;
    private String message;
    private Account account;
}