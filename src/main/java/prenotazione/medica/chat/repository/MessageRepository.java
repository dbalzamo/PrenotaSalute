package prenotazione.medica.chat.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import prenotazione.medica.chat.service.MessageService;
import prenotazione.medica.chat.entity.Message;
import prenotazione.medica.chat.repository.spec.MessageSpecification;

import java.util.List;

/**
 * Accesso ai dati dei {@link Message} (Posta medico-paziente).
 * <p>
 * Supporta query dinamiche tramite {@link MessageSpecification}
 * ({@link JpaSpecificationExecutor}) e metodi JPQL/derivati per conversazione e non letti.
 * Usato da {@link MessageService}.
 * </p>
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {

    /**
     * Recupera l'intera conversazione tra due account, ordinata per data invio crescente.
     * Usata per GET conversazione e per popolare la chat.
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
            "(m.senderId = :userId2 AND m.receiverId = :userId1) " +
            "ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("userId1") Long userId1,
                                   @Param("userId2") Long userId2);

    /** Messaggi non letti dal punto di vista del ricevente (per mark as read). */
    List<Message> findByReceiverIdAndReadFalse(Long receiverId);

    /** Conteggio totale messaggi non letti per un utente (badge Posta). */
    int countByReceiverIdAndReadFalse(Long receiverId);

    /** Conteggio non letti da un determinato mittente verso il ricevente (badge per conversazione in lista chat). */
    int countByReceiverIdAndSenderIdAndReadFalse(Long receiverId, Long senderId);

    /**
     * Ultimo messaggio nella conversazione tra due account (per anteprima lista chat).
     * Richiede {@link Pageable} con size 1 per limitare il risultato.
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
            "(m.senderId = :userId2 AND m.receiverId = :userId1) " +
            "ORDER BY m.sentAt DESC")
    List<Message> findLatestInConversation(@Param("userId1") Long userId1,
                                           @Param("userId2") Long userId2,
                                           Pageable pageable);
}
