package prenotazione.medica.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

import java.util.List;

/**
 * Risposta di login: dati utente e JWT.
 * <p>
 * Restituito da POST {@code /api/auth/login}. Contiene id, username, email, lista ruoli e token
 * JWT da inviare nell'header o in query per le richieste successive e per l'handshake WebSocket.
 * Il campo {@code cookie} è ignorato in serializzazione JSON ma può essere usato per impostare
 * cookie di sessione lato server se necessario.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Long id;
    private String username;
    private String email;
    private List<String> ruoli;
    private String token;
    @JsonIgnore
    ResponseCookie cookie;
}
