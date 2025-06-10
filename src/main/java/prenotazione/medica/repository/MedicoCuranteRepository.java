package prenotazione.medica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.model.MedicoCurante;

import java.util.Optional;

@Repository
public interface MedicoCuranteRepository extends JpaRepository<MedicoCurante, Long>
{

    Optional<MedicoCurante> findByAccountId(Long accountId);
}