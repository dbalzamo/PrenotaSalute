package prenotazione.medica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configurazione del WebSocket in modalità STOMP per la messaggistica real-time (es. Posta medico-paziente).
 * <p>
 * <b>Ruolo nell'architettura:</b> definisce dove il client si connette ({@code /ws}), come invia
 * messaggi ({@code /app/...}) e come riceve notifiche ({@code /user/queue/messages}). L'handshake
 * usa {@link JwtHandshakeHandler} per autenticare tramite JWT; {@link StompSecurityContextInterceptor}
 * imposta il SecurityContext sui messaggi in arrivo così che i controller possano identificare l'utente.
 * </p>
 * <p>
 * <b>Flusso:</b> il frontend si connette a {@code /ws} (con SockJS), sottoscrive a
 * {@code /user/queue/messages} e invia messaggi a {@code /app/chat.send}. Il broker in-memory
 * inoltra le risposte al destinatario corretto.
 * </p>
 *
 * @see MessageBrokerRegistry – configura i prefissi per destinazioni applicative e per il broker.
 * @see StompEndpointRegistry – registra l'endpoint WebSocket (path, handshake handler, SockJS).
 * @see ChannelRegistration – permette di aggiungere interceptor sul canale dei messaggi in ingresso.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer
{
    private final JwtHandshakeHandler jwtHandshakeHandler;
    private final StompSecurityContextInterceptor stompSecurityContextInterceptor;

    public WebSocketConfig(JwtHandshakeHandler jwtHandshakeHandler,
                           StompSecurityContextInterceptor stompSecurityContextInterceptor) {
        this.jwtHandshakeHandler = jwtHandshakeHandler;
        this.stompSecurityContextInterceptor = stompSecurityContextInterceptor;
    }

    /**
     * Configura il message broker STOMP:
     * <ul>
     *   <li>{@code /app} → prefisso per le destinazioni che il client invia (es. /app/chat.send);</li>
     *   <li>{@code /topic}, {@code /queue} → canali su cui il server invia messaggi;</li>
     *   <li>{@code /user} → prefisso per messaggi privati (es. /user/queue/messages per il destinatario).</li>
     * </ul>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Registra l'endpoint WebSocket. Il client si connette qui (es. con SockJS).
     * L'handshake è gestito da {@link JwtHandshakeHandler} per leggere e validare il JWT (es. da query string).
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(jwtHandshakeHandler)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Aggiunge l'interceptor che imposta il SecurityContext prima che il messaggio arrivi al controller.
     * Necessario perché i messaggi STOMP sono gestiti in un thread diverso da quello della richiesta HTTP.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompSecurityContextInterceptor);
    }
}