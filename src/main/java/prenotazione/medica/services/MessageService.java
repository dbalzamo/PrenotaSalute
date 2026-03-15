package prenotazione.medica.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import prenotazione.medica.dto.MessageDTO;
import prenotazione.medica.dto.ConversazionePreviewDTO;
import prenotazione.medica.dto.PazientePerMessaggioDTO;
import prenotazione.medica.dto.response.MessageResponse;
import prenotazione.medica.model.Account;
import prenotazione.medica.model.Message;
import prenotazione.medica.repository.AccountRepository;
import prenotazione.medica.repository.MessageRepository;

import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione dei messaggi della Posta (comunicazione medico–paziente).
 * <p>
 * <b>Ruolo nell'architettura:</b> unico punto di business logic per messaggi: invio (persistenza +
 * notifica WebSocket), recupero conversazioni, marcatura come letti, conteggio non letti e
 * costruzione anteprime per la lista chat del medico. Invocato da {@link prenotazione.medica.controller.ChatController}
 * (REST e WebSocket) e da {@link prenotazione.medica.controller.MedicoCuranteController} per GET conversazioni.
 * </p>
 * <p>
 * <b>Flusso invio:</b> il controller imposta il senderId (da Principal o SecurityContext), poi
 * chiama {@link #sendMessage}. Il messaggio viene salvato, convertito in {@link MessageResponse}
 * e inviato al destinatario tramite {@link SimpMessagingTemplate#convertAndSendToUser} su
 * {@code /queue/messages}, così il client riceve l'evento in tempo reale.
 * </p>
 *
 * @see SimpMessagingTemplate – componente Spring per inviare messaggi su canali STOMP (convertAndSendToUser).
 */
@Service
@RequiredArgsConstructor
public class MessageService
{
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;
    private final MedicoCuranteService medicoCuranteService;

    /**
     * Salva il messaggio e lo invia al destinatario via WebSocket. Il senderId deve essere già
     * impostato nel DTO dal controller. Restituisce il messaggio salvato (con id e sentAt) e notifica
     * il destinatario su {@code /user/queue/messages} tramite il principal name (username).
     */
    public MessageResponse sendMessage(MessageDTO messageDTO)
    {
        Message message = new Message();
        message.setSenderId(messageDTO.getSenderId());
        message.setReceiverId(messageDTO.getReceiverId());
        message.setContent(messageDTO.getContent());
        // id, sentAt, read lasciati ai valori di default (id generato dal DB, sentAt = now, read = false)

        Message saved = messageRepository.save(message);

        MessageResponse response = toResponse(saved);
        // Destinazione WebSocket: Spring usa il principal name (username) per /user/queue/messages
        String receiverUsername = accountRepository.findById(messageDTO.getReceiverId())
                .map(Account::getUsername)
                .orElse(null);
        if (receiverUsername != null) {
            messagingTemplate.convertAndSendToUser(
                    receiverUsername,
                    "/queue/messages",
                    response
            );
        }

        return response;
    }


    private MessageResponse toResponse(Message message)
    {
        return modelMapper.map(message, MessageResponse.class);
    }


    /**
     * Restituisce la conversazione tra due account (userId1 e userId2), ordinata per data. Usata
     * da GET /api/messages/conversation.
     */
    public List<MessageResponse> getConversations(Long userId1, Long userId2)
    {
        return messageRepository.findConversation(userId1, userId2)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }


    /**
     * Segna come letti tutti i messaggi ricevuti da {@code receiverId} e inviati da {@code senderId}.
     * Invocato quando il destinatario apre la conversazione (PUT /api/messages/read).
     */
    public void markAsRead(Long senderId, Long receiverId) {
        List<Message> unread = messageRepository
                .findByReceiverIdAndReadFalse(receiverId)
                .stream()
                .filter(m -> m.getSenderId().equals(senderId))
                .collect(Collectors.toList());

        unread.forEach(m -> m.setRead(true));
        messageRepository.saveAll(unread);
    }


    /**
     * Conteggio messaggi non letti per un utente (id account). Usato per il badge Posta (GET /api/messages/unread/count).
     */
    public int getUnreadCount(Long userId) {
        return messageRepository.countByReceiverIdAndReadFalse(userId);
    }

    private static final int PREVIEW_MAX_LEN = 50;

    /**
     * Anteprima conversazioni per il medico: per ogni paziente restituisce ultimo messaggio (snippet),
     * data/ora e conteggio non letti. Usato da GET /api/v1/medici-curanti/conversazioni per la lista chat.
     */
    public List<ConversazionePreviewDTO> getConversationPreviewsForMedico(Long medicoAccountId) {
        List<PazientePerMessaggioDTO> pazienti =
                medicoCuranteService.findPazientiForMessaging(medicoAccountId);
        List<ConversazionePreviewDTO> result = new ArrayList<>();
        for (PazientePerMessaggioDTO p : pazienti) {
            if (p.getAccountId() == null) continue;
            List<Message> latest = messageRepository.findLatestInConversation(
                    medicoAccountId, p.getAccountId(), PageRequest.of(0, 1));
            String preview = null;
            String lastAt = null;
            if (!latest.isEmpty()) {
                Message last = latest.get(0);
                String content = last.getContent();
                preview = content == null ? "" : content.length() <= PREVIEW_MAX_LEN
                        ? content
                        : content.substring(0, PREVIEW_MAX_LEN) + "…";
                lastAt = last.getSentAt() != null ? last.getSentAt().toString() : null;
            }
            int unread = messageRepository.countByReceiverIdAndSenderIdAndReadFalse(medicoAccountId, p.getAccountId());
            result.add(new ConversazionePreviewDTO(
                    p.getId(),
                    p.getNome(),
                    p.getCognome(),
                    p.getAccountId(),
                    preview != null ? preview : "",
                    lastAt != null ? lastAt : "",
                    unread
            ));
        }
        result.sort((a, b) -> {
            if (a.getLastMessageAt() == null || a.getLastMessageAt().isEmpty()) return 1;
            if (b.getLastMessageAt() == null || b.getLastMessageAt().isEmpty()) return -1;
            return b.getLastMessageAt().compareTo(a.getLastMessageAt());
        });
        return result;
    }
}