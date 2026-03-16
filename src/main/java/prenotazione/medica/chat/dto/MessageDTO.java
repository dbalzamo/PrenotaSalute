package prenotazione.medica.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.chat.api.ChatController;
import prenotazione.medica.chat.dto.response.MessageResponse;

/**
 * DTO per il payload di un messaggio (invio via WebSocket o REST).
 * <p>
 * Usato dal client quando invia su {@code /app/chat.send} (WebSocket) o POST {@code /api/messages}
 * (REST). Il {@code senderId} viene impostato dal backend (da Principal o SecurityContext) in
 * {@link ChatController#sendMessage}; il client può inviare 0 o
 * ignorare il campo. Restituito come {@link MessageResponse}
 * dopo il salvataggio e l'invio al destinatario.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO
{
    private Long senderId;
    private Long receiverId;
    private String content;
}