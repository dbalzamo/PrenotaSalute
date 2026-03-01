package prenotazione.medica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Anteprima di una conversazione per la lista chat del medico (stile WhatsApp).
 * <p>
 * Restituito da GET {@code /api/medico/conversazioni}. Contiene i dati del paziente, un snippet
 * dell'ultimo messaggio, la data/ora e il conteggio messaggi non letti da quel paziente verso il
 * medico. Usato dal frontend per mostrare la sidebar della Posta senza caricare l'intera conversazione.
 * </p>
 *
 * @see prenotazione.medica.services.MessageService#getConversationPreviewsForMedico
 * @see prenotazione.medica.controller.MedicoCuranteController#getConversazioni
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversazionePreviewDTO {
    private Long id;
    private String nome;
    private String cognome;
    /** ID account per messaggistica. */
    private Long accountId;
    /** Anteprima ultimo messaggio (snippet, max ~50 caratteri). */
    private String lastMessagePreview;
    /** Data/ora ultimo messaggio (ISO-8601). */
    private String lastMessageAt;
    /** Numero messaggi non letti da questo paziente verso il medico. */
    private int unreadCount;
}
