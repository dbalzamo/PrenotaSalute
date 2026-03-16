package prenotazione.medica.shared.security;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import prenotazione.medica.auth.jwt.JwtService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Handshake WebSocket: imposta il Principal dalla sessione oppure dall'header
 * {@code Authorization: Bearer <token>} della richiesta di connessione a /ws.
 */
@Component
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    private static final String AUTHORIZATION = "Authorization";

    private final JwtService jwtService;

    public JwtHandshakeHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler handler, Map<String, Object> attributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            return () -> auth.getName();
        }
        List<String> authHeaders = request.getHeaders().get(AUTHORIZATION);
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String token = jwtService.getJwtFromAuthorizationHeader(authHeaders.get(0));
            if (token != null && jwtService.validateJwtToken(token)) {
                String username = jwtService.getUserNameFromJwtToken(token);
                return () -> username;
            }
        }
        return null;
    }
}
