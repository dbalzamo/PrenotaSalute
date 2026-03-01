package prenotazione.medica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.model.Paziente;

import java.util.List;
import java.util.Optional;

/**
 * Accesso ai dati dei {@link prenotazione.medica.model.Paziente}.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link prenotazione.medica.services.PazienteService} per
 * recuperare il paziente dall'account loggato e per aggiornare medico curante; da
 * {@link prenotazione.medica.services.MedicoCuranteService#findPazientiForMessaging} per l'elenco
 * pazienti del medico (messaggistica e lista chat).
 * </p>
 */
@Repository
public interface PazienteRepository extends JpaRepository<Paziente, Long>
{
    /** Recupera il paziente associato a un account (per risalire da utente loggato a profilo paziente). */
    Optional<Paziente> findByAccountId(Long accountId);

    /** Elenco pazienti associati a un medico curante (per messaggistica e dashboard medico). */
    List<Paziente> findByMedicoCurante_Id(Long medicoCuranteId);
}