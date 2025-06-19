package prenotazione.medica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.enums.EStatoRichiesta;
import prenotazione.medica.model.RichiestaMedica;

import java.util.List;

@Repository
public interface RichiestaMedicaRepository extends JpaRepository<RichiestaMedica, Long>
{

    List<RichiestaMedica> findAllByStatoAndMedicoCurante_Id(EStatoRichiesta stato, Long medicoId);
    List<RichiestaMedica> findAllByStatoAndPaziente_Id(EStatoRichiesta stato, Long pazienteId);
    List<RichiestaMedica> findAllByStato(EStatoRichiesta stato);
}
