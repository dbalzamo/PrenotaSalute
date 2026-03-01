package prenotazione.medica.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import prenotazione.medica.services.UserDetailsServiceImpl;

/**
 * Interceptor per i messaggi STOMP in ingresso: imposta il {@link org.springframework.security.core.context.SecurityContext}
 * con l'utente derivato dal Principal della sessione WebSocket.
 * <p>
 * <b>Ruolo nell'architettura:</b> i messaggi inviati su {@code /app/chat.send} sono gestiti in un
 * thread del message broker, dove il SecurityContext della richiesta HTTP non è presente. Questo
 * interceptor viene eseguito in {@link #preSend} prima che il messaggio raggiunga il
 * {@link prenotazione.medica.controller.ChatController}: carica i dettagli utente tramite
 * {@link UserDetailsServiceImpl#loadUserByUsername} e li imposta nel SecurityContext, così che
 * eventuali codici che usano {@link prenotazione.medica.security.utils.SecurityUtils#getCurrentAccountId()}
 * possano funzionare. In questo progetto il controller usa direttamente il {@link java.security.Principal}
 * iniettato dal messaggio, ma l'interceptor mantiene coerenza con il resto dello stack sicurezza.
 * </p>
 *
 * @see ChannelInterceptor – interfaccia Spring per intervenire sul flusso dei messaggi (preSend, postSend, ecc.).
 * @see StompHeaderAccessor – wrapper che espone comando STOMP, header e Principal del messaggio.
 */
@Component
public class StompSecurityContextInterceptor implements ChannelInterceptor {

    private final UserDetailsServiceImpl userDetailsService;

    public StompSecurityContextInterceptor(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Invocato prima che il messaggio venga consegnato al controller. Per messaggi SEND e SUBSCRIBE,
     * se il messaggio ha un Principal (impostato in handshake da {@link JwtHandshakeHandler}) e il
     * SecurityContext è ancora vuoto, carica l'utente e imposta l'autenticazione.
     *
     * @param message messaggio STOMP in transito
     * @param channel  canale di destinazione
     * @return il messaggio inalterato (sempre restituito per far proseguire il flusso)
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            java.security.Principal principal = accessor.getUser();
            if (principal != null && principal.getName() != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    var userDetails = userDetailsService.loadUserByUsername(principal.getName());
                    var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception ignored) {
                    // utente non trovato: lascia SecurityContext vuoto
                }
            }
        }
        return message;
    }
}
