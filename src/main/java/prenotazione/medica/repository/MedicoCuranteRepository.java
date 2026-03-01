package prenotazione.medica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.model.MedicoCurante;

import java.util.Optional;

/**
 * Accesso ai dati dei {@link prenotazione.medica.model.MedicoCurante}.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link prenotazione.medica.services.MedicoCuranteService}
 * per recuperare il medico dall'account loggato, per l'elenco medici in signup e per la gestione
 * pazienti. I controller medico usano il service che a sua volta usa questo repository.
 * </p>
 */
@Repository
public interface MedicoCuranteRepository extends JpaRepository<MedicoCurante, Long>
{
    /** Recupera il medico curante associato a un account (per risalire da utente loggato a profilo medico). */
    Optional<MedicoCurante> findByAccountId(Long accountId);
}