package prenotazione.medica.impegnativa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import prenotazione.medica.impegnativa.entity.Impegnativa;
import prenotazione.medica.impegnativa.service.ImpegnativaService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Accesso ai dati delle {@link Impegnativa}.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link ImpegnativaService}
 * per salvare e recuperare impegnative. Spring Data fornisce findById, save, delete, findAll senza
 * implementazione esplicita.
 * </p>
 */
@Repository
public interface ImpegnativaRepository extends JpaRepository<Impegnativa, Long> {

    Optional<Impegnativa> findByRichiestaMedica_Id(Long richiestaMedicaId);

    @Query("SELECT DISTINCT i FROM Impegnativa i JOIN FETCH i.richiestaMedica WHERE i.richiestaMedica.id IN :ids")
    List<Impegnativa> findByRichiestaMedica_IdIn(@Param("ids") Collection<Long> ids);

    @Query("SELECT DISTINCT i FROM Impegnativa i "
            + "JOIN FETCH i.paziente JOIN FETCH i.medicoCurante LEFT JOIN FETCH i.prestazioneSanitaria "
            + "WHERE i.id = :id")
    Optional<Impegnativa> findWithDetailsById(@Param("id") Long id);
}
