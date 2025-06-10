package prenotazione.medica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.model.Impegnativa;

@Repository
public interface ImpegnativaRepository extends JpaRepository<Impegnativa, Long>
{

}
