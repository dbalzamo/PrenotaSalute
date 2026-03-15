package prenotazione.medica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Risposta di login: dati utente e JWT da inviare nell'header {@code Authorization: Bearer <token>}.
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
}
