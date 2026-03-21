package prenotazione.medica.impegnativa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.impegnativa.entity.Impegnativa;
import prenotazione.medica.impegnativa.service.ImpegnativaService;

/**
 * Accesso ai dati delle {@link Impegnativa}.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link ImpegnativaService}
 * per salvare e recuperare impegnative. Spring Data fornisce findById, save, delete, findAll senza
 * implementazione esplicita.
 * </p>
 */
@Repository
public interface ImpegnativaRepository extends JpaRepository<Impegnativa, Long>
{
}
