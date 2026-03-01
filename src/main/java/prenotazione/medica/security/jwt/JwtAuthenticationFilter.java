package prenotazione.medica.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import prenotazione.medica.services.UserDetailsServiceImpl;

/**
 * Filtro Spring Security che esegue l'autenticazione JWT su ogni richiesta HTTP.
 * <p>
 * <b>Ruolo nell'architettura:</b> inserito nella catena prima di {@link UsernamePasswordAuthenticationFilter}.
 * Estrae il JWT dalla richiesta (tramite {@link JwtService#getJwtFromRequest}), lo valida, ricava
 * lo username e carica i {@link UserDetails} con {@link UserDetailsServiceImpl#loadUserByUsername},
 * poi imposta l'{@link org.springframework.security.core.Authentication} nel
 * {@link org.springframework.security.core.context.SecurityContextHolder}. I controller e
 * SecurityUtils possono così ottenere l'utente corrente. Per le richieste WebSocket l'autenticazione
 * avviene in handshake ({@link prenotazione.medica.config.JwtHandshakeHandler}).
 * </p>
 *
 * @see OncePerRequestFilter – garantisce una sola esecuzione per richiesta.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt == null) {
                logger.debug("JWT is null for request URI: {}", request.getRequestURI());
            } else if (!jwtService.validateJwtToken(jwt)) {
                logger.warn("JWT validation failed for request URI: {}", request.getRequestURI());
            }
            if (jwt != null && jwtService.validateJwtToken(jwt)) {
                String username = jwtService.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, // INFORMAZIONI AGGIUNTIVE
                        userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {} - {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }


    private String parseJwt(HttpServletRequest request)
    {
        return jwtService.getJwtFromRequest(request);
    }

}