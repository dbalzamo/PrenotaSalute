package prenotazione.medica.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Richiesta di login (credenziali).
 * <p>
 * Body di POST {@code /api/auth/login}. Usato da {@link prenotazione.medica.controller.AuthController}
 * per invocare l'autenticazione e restituire il JWT e i dati utente ({@link prenotazione.medica.dto.response.AuthResponse}).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    private String username;
    private String password;

}
