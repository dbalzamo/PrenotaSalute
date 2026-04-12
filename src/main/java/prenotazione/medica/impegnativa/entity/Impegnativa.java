package prenotazione.medica.impegnativa.entity;

import com.prenotasalute.commons.entity.EntityBase;
import jakarta.persistence.*;
import prenotazione.medica.impegnativa.service.ImpegnativaService;
import prenotazione.medica.impegnativa.api.ImpegnativaController;
import prenotazione.medica.prestazioneSanitaria.entity.PrestazioneSanitaria;
import prenotazione.medica.paziente.entity.Paziente;
import prenotazione.medica.medico.entity.MedicoCurante;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.shared.enums.EPrioritàPrescrizione;
import prenotazione.medica.shared.enums.ETipoRichiesta;

/**
 * Entità JPA che rappresenta un'impegnativa (prescrizione) emessa dal medico per il paziente.
 * <p>
 * <b>Ruolo nell'architettura:</b> l'impegnativa è creata/gestita da
 * {@link ImpegnativaService} ed esposta da
 * {@link ImpegnativaController}. Può essere collegata a una
 * {@link PrestazioneSanitaria} (uno-a-uno). Regione, codice NRE e priorità sono usati per
 * identificazione e flussi amministrativi.
 * </p>
 *
 * @see EPrioritàPrescrizione – priorità della prescrizione.
 * @see ETipoRichiesta – tipo di ricetta (es. farmaco, visita).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="impegnativa")
public class Impegnativa extends EntityBase {

	@Column(name = "regione", length = 50, nullable = false)
	private String regione;

	@Column(name = "codice_nre", unique = true, nullable = false, length = 16)
	private String codiceNRE;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_ricetta", nullable = false, length = 50)
	private ETipoRichiesta tipoRicetta;

	@Enumerated(EnumType.STRING)
	@Column(name = "priorita")
	private EPrioritàPrescrizione priorità;

	@ManyToOne
	@JoinColumn(name = "id_paziente")
	private Paziente paziente;
	
	@ManyToOne
	@JoinColumn(name = "id_medico_curante")
	private MedicoCurante medicoCurante;

	/** Richiesta medica da cui è stata generata (opzionale per dati legacy). */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_richiesta_medica")
	private RichiestaMedica richiestaMedica;

	@OneToOne(mappedBy = "impegnativa", cascade = CascadeType.ALL)
	private PrestazioneSanitaria prestazioneSanitaria;
}
