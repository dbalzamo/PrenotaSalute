package prenotazione.medica.paziente.repository;

import prenotazione.medica.medico.service.MedicoCuranteService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.paziente.service.PazienteService;
import prenotazione.medica.paziente.entity.Paziente;

import java.util.List;
import java.util.Optional;

/**
 * Accesso ai dati dei {@link Paziente}.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link PazienteService} per
 * recuperare il paziente dall'account loggato e per aggiornare medico curante; da
 * {@link MedicoCuranteService #findPazientiForMessaging} per l'elenco
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

