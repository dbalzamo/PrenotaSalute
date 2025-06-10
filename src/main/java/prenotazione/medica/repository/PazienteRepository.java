package prenotazione.medica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.model.Paziente;

import java.util.Optional;

@Repository
public interface PazienteRepository extends JpaRepository<Paziente, Long>
{
    Optional<Paziente> findByAccountId(Long accountId);
}