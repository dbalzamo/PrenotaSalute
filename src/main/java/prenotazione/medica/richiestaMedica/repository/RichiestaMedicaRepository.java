package prenotazione.medica.richiestaMedica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;
import prenotazione.medica.richiestaMedica.service.RichiestaMedicaService;
import prenotazione.medica.shared.enums.EStatoRichiesta;
import prenotazione.medica.richiestaMedica.repository.spec.RichiestaMedicaSpecification;

import java.time.Instant;
import java.util.List;

/**
 * Accesso ai dati delle {@link RichiestaMedica}.
 * <p>
 * Supporta query dinamiche tramite {@link RichiestaMedicaSpecification}
 * ({@link JpaSpecificationExecutor#findAll(org.springframework.data.jpa.domain.Specification, org.springframework.data.domain.Sort)})
 * e metodi derivati per i filtri più comuni. Usato da {@link RichiestaMedicaService}.
 * </p>
 */
@Repository
public interface RichiestaMedicaRepository extends JpaRepository<RichiestaMedica, Long>, JpaSpecificationExecutor<RichiestaMedica> {

    /**
     * Marca come SCADUTA le richieste accettate la cui data di accettazione è precedente a {@code limite}.
     * Invocato da uno job schedulato (es. {@link org.springframework.scheduling.annotation.Scheduled}).
     */
    @Modifying
    @Query("""
        UPDATE RichiestaMedica r 
        SET r.stato = prenotazione.medica.shared.enums.EStatoRichiesta.SCADUTA
        WHERE r.stato = prenotazione.medica.shared.enums.EStatoRichiesta.ACCETTATA
        AND r.dataAccettazione <= :limite
    """)
    int scadutaRichieste(@Param("limite") Instant limite);

    List<RichiestaMedica> findByStatoAndDataAccettazioneBefore(EStatoRichiesta stato, Instant limite);
}
