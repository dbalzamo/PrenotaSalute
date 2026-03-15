package prenotazione.medica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.model.PrestazioneSanitaria;

/**
 * Repository JPA per l'entità PrestazioneSanitaria.
 */
@Repository
public interface PrestazioneSanitariaRepository extends JpaRepository<PrestazioneSanitaria, Long> {
}
