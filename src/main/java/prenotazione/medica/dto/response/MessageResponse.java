package prenotazione.medica.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO di risposta per un messaggio (API REST e payload WebSocket).
 * <p>
 * Restituito da GET conversazione, POST messaggio e inviato al destinatario su
 * {@code /user/queue/messages}. Serializzato in JSON con data in formato ISO. Usato dal frontend
 * per mostrare la chat e aggiornare la lista in tempo reale.
 * </p>
 *
 * @see prenotazione.medica.services.MessageService#sendMessage
 * @see prenotazione.medica.controller.ChatController
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse
{
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentAt;
    private boolean read;
}