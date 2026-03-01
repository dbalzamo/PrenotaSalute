package prenotazione.medica.security.jwt;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import prenotazione.medica.model.UserDetailsImpl;

/**
 * Servizio per la generazione, validazione e estrazione del JWT.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link JwtAuthenticationFilter} per validare il token
 * su ogni richiesta HTTP, da {@link JwtHandshakeHandler} (config) per l'handshake WebSocket, e da
 * {@link prenotazione.medica.services.AccountService} per generare il token al login. Il token può
 * essere letto da header Authorization (Bearer), da cookie o da query param "token" (per /ws).
 * Chiave e scadenza sono configurati in application.properties (jwt.secret, jwt.expirationMs).
 * </p>
 *
 * @see io.jsonwebtoken.Jwts – libreria JJWT per creare e parsare JWT.
 */
@Service
public class JwtService
{
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    // Recupero il valore dall'application.properties.
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;
    @Value("${jwt.jwtCookieName}")
    private String jwtCookie;


    /**
     * Estrae il JWT dalla richiesta: prima dall'header Authorization (Bearer),
     * poi dal cookie, infine dal query param "token" (per handshake WebSocket /ws).
     */
    public String getJwtFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        String fromCookie = getJwtFromCookies(request);
        if (fromCookie != null) return fromCookie;
        if (request.getRequestURI() != null && request.getRequestURI().contains("/ws") && request.getParameter("token") != null) {
            return request.getParameter("token");
        }
        return null;
    }

    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            return  cookie.getValue();
        } else {
            return null;
        }
    }


    public ResponseCookie generateCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt)
                .path("/prenotazione-medica")
                .maxAge(24 * 60 * 60)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .build();
        return cookie;
    }


    public ResponseCookie getCleanJwtCookie() {
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, null).path("/prenotazione-medica").build();
        return cookie;
    }


    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody().getSubject();
    }


    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }


    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }


    public String generateTokenFromUsername(String username) {
        return Jwts.builder().setSubject(username).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256).compact();
    }


}
