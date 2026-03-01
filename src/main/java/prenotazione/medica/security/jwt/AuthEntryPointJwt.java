package prenotazione.medica.security.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Entry point invocato quando un utente non autenticato tenta di accedere a una risorsa protetta.
 * <p>
 * <b>Ruolo nell'architettura:</b> configurato in {@link prenotazione.medica.security.config.WebSecurityConfig}
 * tramite {@code exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))}.
 * Invece di reindirizzare al login, restituisce una risposta JSON 401 con messaggio e path, adatta
 * per API REST consumate da frontend SPA.
 * </p>
 *
 * @see AuthenticationEntryPoint – interfaccia Spring Security per gestire richieste non autenticate.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        logger.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
