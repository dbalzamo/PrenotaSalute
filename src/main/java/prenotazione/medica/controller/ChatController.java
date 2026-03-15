package prenotazione.medica.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import prenotazione.medica.dto.MessageDTO;
import prenotazione.medica.dto.response.MessageResponse;
import prenotazione.medica.exception.ResourceNotFoundException;
import prenotazione.medica.exception.UnauthorizedException;
import prenotazione.medica.repository.AccountRepository;
import prenotazione.medica.security.utils.SecurityUtils;
import prenotazione.medica.services.MessageService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Controller per la messaggistica (Posta): endpoint REST e handler WebSocket.
 * <p>
 * <b>Ruolo nell'architettura:</b> espone le API REST per conversazione, invio messaggio (fallback),
 * mark as read e conteggio non letti; gestisce inoltre l'invio via WebSocket con
 * {@link org.springframework.messaging.handler.annotation.MessageMapping} su {@code /app/chat.send}.
 * Il client si connette a {@code /ws}, sottoscrive a {@code /user/queue/messages} e invia su
 * {@code /app/chat.send}. Per il WebSocket l'identità del mittente viene ricavata dal
 * {@link Principal} impostato in handshake da {@link prenotazione.medica.config.JwtHandshakeHandler},
 * non da {@link SecurityUtils}, per evitare "Account non autenticato" quando il SecurityContext
 * non è propagato nel thread del message broker.
 * </p>
 *
 * @see MessageMapping – mappa destinazione STOMP /app/chat.send al metodo sendMessage.
 * @see Payload – estrae il body del messaggio STOMP nel DTO.
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final AccountRepository accountRepository;

    /**
     * Handler per l'invio di un messaggio via WebSocket (destinazione /app/chat.send).
     * Spring inietta il Principal dalla sessione STOMP; l'id account si ricava dallo username.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageDTO messageDTO, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new UnauthorizedException("websocket.unauthorized");
        }
        Long currentAccountId = accountRepository
                .findByUsername(principal.getName())
                .map(acc -> acc.getId())
                .orElseThrow(() -> new ResourceNotFoundException("account.notfound.for", principal.getName()));
        messageDTO.setSenderId(currentAccountId);
        messageService.sendMessage(messageDTO);
    }

    /**
     * REST: invio messaggio (fallback quando il WebSocket non è disponibile).
     * Body: { "receiverId": number, "content": string }. Richiede autenticazione.
     */
    @PostMapping("/api/messages")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> sendMessageRest(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("receiverId") == null || body.get("content") == null) {
            return ResponseEntity.badRequest().build();
        }
        Long receiverId = body.get("receiverId") instanceof Number
                ? ((Number) body.get("receiverId")).longValue()
                : Long.parseLong(String.valueOf(body.get("receiverId")));
        String content = String.valueOf(body.get("content"));
        Long senderId = SecurityUtils.getCurrentAccountId();
        MessageDTO dto = new MessageDTO(senderId, receiverId, content);
        MessageResponse response = messageService.sendMessage(dto);
        return ResponseEntity.ok(response);
    }

    /**
     * REST: carica lo storico della conversazione tra userId1 e userId2. Consentito solo se
     * l'utente corrente è uno dei due (controllo su SecurityUtils.getCurrentAccountId()).
     */
    @GetMapping("/api/messages/conversation")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        Long current = SecurityUtils.getCurrentAccountId();
        if (!current.equals(userId1) && !current.equals(userId2)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(messageService.getConversations(userId1, userId2));
    }

    /**
     * REST: segna come letti i messaggi da senderId verso receiverId. Solo il destinatario (receiverId)
     * può eseguire l'operazione.
     */
    @PutMapping("/api/messages/read")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@RequestParam Long senderId, @RequestParam Long receiverId) {
        if (!SecurityUtils.getCurrentAccountId().equals(receiverId)) {
            return ResponseEntity.status(403).build();
        }
        messageService.markAsRead(senderId, receiverId);
        return ResponseEntity.ok().build();
    }

    /**
     * REST: conteggio messaggi non letti per l'utente indicato. Restituito solo se userId coincide
     * con l'account corrente (per il badge Posta).
     */
    @GetMapping("/api/messages/unread/count")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> getUnreadCount(@RequestParam Long userId) {
        if (!SecurityUtils.getCurrentAccountId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(messageService.getUnreadCount(userId));
    }
}