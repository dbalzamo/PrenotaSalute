package prenotazione.medica.richiestaMedica.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.prenotasalute.commons.entity.EntityBase;
import prenotazione.medica.paziente.entity.Paziente;
import prenotazione.medica.medico.entity.MedicoCurante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.richiestaMedica.service.RichiestaMedicaService;
import prenotazione.medica.richiestaMedica.api.RichiestaMedicaController;
import prenotazione.medica.shared.enums.EStatoRichiesta;
import prenotazione.medica.shared.enums.ETipoRichiesta;

/**
 * Entità JPA che rappresenta una richiesta medica inviata da un paziente al proprio medico curante.
 * <p>
 * <b>Ruolo nell'architettura:</b> il paziente crea richieste dalla dashboard; il medico le visualizza,
 * le accetta o rifiuta. Il flusso è gestito da {@link RichiestaMedicaService}
 * e esposto da {@link RichiestaMedicaController}. Lo stato
 * ({@link EStatoRichiesta}) e il tipo ({@link ETipoRichiesta}) determinano filtri e notifiche.
 * </p>
 *
 * @see EStatoRichiesta – IN_ATTESA, ACCETTATA, RIFIUTATA.
 * @see ETipoRichiesta – tipologia della richiesta (es. visita, esame).
 */
@Entity
@Table(name="richiesta_medica")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RichiestaMedica extends EntityBase {

	@Column(name = "data_richiesta", nullable = false)
	private Date dataEmissione;

	/**
	 * Data e ora di accettazione da parte del medico.
	 * @Temporal(TemporalType.TIMESTAMP) indica a JPA di salvare data e ora nel DB.
	 */
	@Column(name = "data_accettazione")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dataAccettazione;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_richiesta")
	private ETipoRichiesta tipoRichiesta;

	@Enumerated(EnumType.STRING)
	@Column(name = "stato_richiesta")
	private EStatoRichiesta stato;
	
	@Column(name = "descrizione", nullable = false)
	private String descrizione;
	
	@ManyToOne
	@JoinColumn(name = "id_paziente")
	@JsonBackReference
	private Paziente paziente;
	
	@ManyToOne
	@JoinColumn(name = "id_medico_curante")
	@JsonBackReference
	private MedicoCurante medicoCurante;
}