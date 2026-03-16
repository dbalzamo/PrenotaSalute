package prenotazione.medica.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.auth.entity.Account;
import prenotazione.medica.chat.repository.MessageRepository;
import prenotazione.medica.chat.dto.response.MessageResponse;
import prenotazione.medica.chat.service.MessageService;

import java.time.LocalDateTime;

/**
 * Entità JPA che rappresenta un messaggio nella Posta (comunicazione medico–paziente).
 * <p>
 * <b>Ruolo nell'architettura:</b> i messaggi sono creati dal
 * {@link MessageService} (via REST o WebSocket), persistiti tramite
 * {@link MessageRepository} e inviati in tempo reale al destinatario
 * tramite {@link org.springframework.messaging.simp.SimpMessagingTemplate}. I campi
 * {@code senderId} e {@code receiverId} sono id degli {@link Account} (non di Paziente/MedicoCurante),
 * così da identificare univocamente mittente e destinatario indipendentemente dal ruolo.
 * </p>
 *
 * @see MessageRepository – query per conversazione, ultimo messaggio, conteggio non letti.
 * @see MessageResponse – DTO esposto alle API e al WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "messages")
public class Message
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id dell'account mittente (riferimento a {@link Account#id}). */
    private Long senderId;
    /** Id dell'account destinatario (riferimento a {@link Account#id}). */
    private Long receiverId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime sentAt = LocalDateTime.now();
    /** Se il destinatario ha letto il messaggio; usato per badge e conteggi non letti. */
    @Column(name = "is_read")
    private boolean read = false;
}
