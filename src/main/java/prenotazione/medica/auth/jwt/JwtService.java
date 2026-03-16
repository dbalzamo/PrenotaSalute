package prenotazione.medica.auth.jwt;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Servizio per generazione, validazione ed estrazione del JWT.
 * Il token viene letto solo dall'header {@code Authorization: Bearer <token>}.
 * Chiave e scadenza in application.properties (jwt.secret, jwt.expirationMs).
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    /**
     * Estrae il JWT dall'header Authorization (Bearer &lt;token&gt;). Restituisce null se assente o malformato.
     */
    public String getJwtFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }

    /**
     * Estrae il JWT dall'header Authorization della richiesta di handshake WebSocket.
     */
    public String getJwtFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private Key signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateJwtToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(signingKey()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.debug("JWT malformato: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.debug("JWT scaduto: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.debug("JWT non supportato: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.debug("JWT claims vuoti: {}", e.getMessage());
        }
        return false;
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey())
                .compact();
    }
}
