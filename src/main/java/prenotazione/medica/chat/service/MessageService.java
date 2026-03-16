package prenotazione.medica.chat.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import prenotazione.medica.chat.dto.response.MessageResponse;
import prenotazione.medica.chat.api.ChatController;
import prenotazione.medica.chat.dto.ConversazionePreviewDTO;
import prenotazione.medica.chat.dto.MessageDTO;
import prenotazione.medica.chat.entity.Message;
import prenotazione.medica.chat.repository.MessageRepository;
import prenotazione.medica.chat.dto.PazientePerMessaggioDTO;
import prenotazione.medica.auth.entity.Account;
import prenotazione.medica.auth.repository.AccountRepository;
import prenotazione.medica.medico.service.MedicoCuranteService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import prenotazione.medica.chat.repository.spec.MessageSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione dei messaggi della Posta (comunicazione medico–paziente).
 * <p>
 * <b>Ruolo nell'architettura:</b> unico punto di business logic per messaggi: invio (persistenza +
 * notifica WebSocket), recupero conversazioni, marcatura come letti, conteggio non letti e
 * costruzione anteprime per la lista chat del medico. Invocato da {@link ChatController}
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
     * da GET /api/messages/conversation. Utilizza {@link MessageSpecification#conversationBetween}.
     */
    public List<MessageResponse> getConversations(Long userId1, Long userId2) {
        var spec = MessageSpecification.conversationBetween(userId1, userId2);
        List<Message> list = messageRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "sentAt"));
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }


    /**
     * Segna come letti tutti i messaggi ricevuti da {@code receiverId} e inviati da {@code senderId}.
     * Invocato quando il destinatario apre la conversazione (PUT /api/messages/read).
     * Utilizza {@link MessageSpecification#unreadByReceiverAndSender}.
     */
    public void markAsRead(Long senderId, Long receiverId) {
        var spec = MessageSpecification.unreadByReceiverAndSender(receiverId, senderId);
        List<Message> unread = messageRepository.findAll(spec);
        unread.forEach(m -> m.setRead(true));
        messageRepository.saveAll(unread);
    }


    /**
     * Conteggio messaggi non letti per un utente (id account). Usato per il badge Posta (GET /api/messages/unread/count).
     * Utilizza {@link MessageSpecification#unreadByReceiver}.
     */
    public int getUnreadCount(Long userId) {
        return (int) messageRepository.count(MessageSpecification.unreadByReceiver(userId));
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
            var spec = MessageSpecification.conversationBetween(medicoAccountId, p.getAccountId());
            List<Message> latest = messageRepository.findAll(spec,
                    PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "sentAt"))).getContent();
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
            int unread = (int) messageRepository.count(
                    MessageSpecification.unreadByReceiverAndSender(medicoAccountId, p.getAccountId()));
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