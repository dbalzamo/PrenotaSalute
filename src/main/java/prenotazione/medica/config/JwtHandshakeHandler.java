package prenotazione.medica.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import prenotazione.medica.security.jwt.JwtService;

import java.net.URI;
import java.security.Principal;
import java.util.Map;

/**
 * Gestisce l'handshake della connessione WebSocket e imposta il {@link Principal} della sessione
 * a partire dal JWT fornito dal client.
 * <p>
 * <b>Ruolo nell'architettura:</b> quando il frontend si connette a {@code /ws?token=...}, Spring
 * invoca questo handler prima di stabilire la connessione. Il Principal (username) viene associato
 * alla sessione STOMP e usato poi da {@link StompSecurityContextInterceptor} e da
 * {@link prenotazione.medica.controller.ChatController} per identificare l'utente che invia un messaggio.
 * </p>
 * <p>
 * <b>Flusso:</b> (1) Il client apre la connessione con il token in query string. (2) Se il
 * SecurityContext è già popolato (es. da un filtro), si usa il nome dell'autenticazione. (3)
 * Altrimenti si estrae il token dalla query, si valida con {@link JwtService} e si ricava lo
 * username. (4) Si restituisce un Principal che espone tale username; {@code null} se il token
 * è assente o non valido.
 * </p>
 *
 * @see DefaultHandshakeHandler – base di Spring per l'handshake WebSocket.
 * @see ServerHttpRequest – rappresenta la richiesta HTTP di handshake (URI, header, ecc.).
 */
@Component
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtService jwtService;

    public JwtHandshakeHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Determina l'utente (Principal) associato alla sessione WebSocket.
     * Invocato da Spring durante l'handshake per ogni nuova connessione.
     *
     * @param request    richiesta di handshake (contiene l'URI con eventuale query {@code ?token=...})
     * @param handler    handler WebSocket che gestirà la connessione
     * @param attributes attributi della sessione (usati internamente da Spring)
     * @return Principal con nome uguale allo username, oppure {@code null} se non autenticato
     */
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler handler, Map<String, Object> attributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            return () -> auth.getName();
        }
        String token = getTokenFromQuery(request.getURI());
        if (token != null && jwtService.validateJwtToken(token)) {
            String username = jwtService.getUserNameFromJwtToken(token);
            return () -> username;
        }
        return null;
    }

    /**
     * Estrae il valore del parametro {@code token} dalla query string dell'URI.
     */
    private static String getTokenFromQuery(URI uri) {
        if (uri == null) return null;
        String query = uri.getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            int eq = param.indexOf('=');
            if (eq > 0 && "token".equals(param.substring(0, eq).trim())) {
                return param.substring(eq + 1).trim();
            }
        }
        return null;
    }
}
