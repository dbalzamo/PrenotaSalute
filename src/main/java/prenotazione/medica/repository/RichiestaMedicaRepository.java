package prenotazione.medica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import prenotazione.medica.enums.EStatoRichiesta;
import prenotazione.medica.model.RichiestaMedica;

import java.time.Instant;
import java.util.List;

/**
 * Accesso ai dati delle {@link prenotazione.medica.model.RichiestaMedica}.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link prenotazione.medica.services.RichiestaMedicaService}
 * per le operazioni CRUD, per filtrare richieste per stato e per medico/paziente, e per lo job
 * di scadenza richieste accettate oltre un certo limite temporale ({@link #scadutaRichieste}).
 * </p>
 */
@Repository
public interface RichiestaMedicaRepository extends JpaRepository<RichiestaMedica, Long>
{
    List<RichiestaMedica> findAllByStatoAndMedicoCurante_Id(EStatoRichiesta stato, Long medicoId);
    List<RichiestaMedica> findAllByStatoAndPaziente_Id(EStatoRichiesta stato, Long pazienteId);
    List<RichiestaMedica> findAllByPaziente_IdOrderByDataEmissioneDesc(Long pazienteId);
    List<RichiestaMedica> findAllByMedicoCurante_IdOrderByDataEmissioneDesc(Long medicoId);

    /**
     * Marca come SCADUTA le richieste accettate la cui data di accettazione è precedente a {@code limite}.
     * Invocato da uno job schedulato (es. {@link org.springframework.scheduling.annotation.Scheduled}).
     */
    @Modifying
    @Query("""
        UPDATE RichiestaMedica r 
        SET r.stato = prenotazione.medica.enums.EStatoRichiesta.SCADUTA
        WHERE r.stato = prenotazione.medica.enums.EStatoRichiesta.ACCETTATA
        AND r.dataAccettazione <= :limite
    """)
    int scadutaRichieste(@Param("limite") Instant limite);
}
