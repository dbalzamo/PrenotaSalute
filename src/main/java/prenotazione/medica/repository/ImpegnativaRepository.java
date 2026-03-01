package prenotazione.medica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.model.Impegnativa;

/**
 * Accesso ai dati delle {@link prenotazione.medica.model.Impegnativa}.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link prenotazione.medica.services.ImpegnativaService}
 * per salvare e recuperare impegnative. Spring Data fornisce findById, save, delete, findAll senza
 * implementazione esplicita.
 * </p>
 */
@Repository
public interface ImpegnativaRepository extends JpaRepository<Impegnativa, Long>
{
}
