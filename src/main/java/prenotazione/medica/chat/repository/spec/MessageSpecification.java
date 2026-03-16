package prenotazione.medica.chat.repository.spec;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import prenotazione.medica.chat.entity.Message;
import prenotazione.medica.chat.repository.MessageRepository;

/**
 * Specification JPA per query dinamiche su {@link Message} (Posta medico–paziente).
 * <p>
 * Permette di esprimere criteri come "conversazione tra due utenti" e "messaggi non letti"
 * in modo composabile. Usato da {@link MessageRepository}
 * tramite {@link org.springframework.data.jpa.repository.JpaSpecificationExecutor}.
 * </p>
 *
 * @see org.springframework.data.jpa.domain.Specification
 */
public final class MessageSpecification {

    private MessageSpecification() {
    }

    /**
     * Filtra i messaggi appartenenti alla conversazione tra due account (bidirezionale:
     * messaggi inviati da userId1 a userId2 e da userId2 a userId1).
     *
     * @param userId1 id primo account
     * @param userId2 id secondo account
     * @return specification per la conversazione
     */
    public static Specification<Message> conversationBetween(Long userId1, Long userId2) {
        return (root, query, cb) -> {
            Predicate p1 = cb.and(
                    cb.equal(root.get("senderId"), userId1),
                    cb.equal(root.get("receiverId"), userId2)
            );
            Predicate p2 = cb.and(
                    cb.equal(root.get("senderId"), userId2),
                    cb.equal(root.get("receiverId"), userId1)
            );
            return cb.or(p1, p2);
        };
    }

    /**
     * Filtra i messaggi non letti dal punto di vista del destinatario.
     *
     * @param receiverId id account destinatario
     * @return specification per messaggi non letti
     */
    public static Specification<Message> unreadByReceiver(Long receiverId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("receiverId"), receiverId),
                cb.equal(root.get("read"), false)
        );
    }

    /**
     * Filtra i messaggi non letti da un determinato mittente verso il ricevente.
     *
     * @param receiverId id account destinatario
     * @param senderId   id account mittente
     * @return specification per non letti in quella conversazione
     */
    public static Specification<Message> unreadByReceiverAndSender(Long receiverId, Long senderId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("receiverId"), receiverId),
                cb.equal(root.get("senderId"), senderId),
                cb.equal(root.get("read"), false)
        );
    }
}
