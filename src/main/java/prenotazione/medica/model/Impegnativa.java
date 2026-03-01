package prenotazione.medica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.enums.EPrioritàPrescrizione;
import prenotazione.medica.enums.ETipoRichiesta;

/**
 * Entità JPA che rappresenta un'impegnativa (prescrizione) emessa dal medico per il paziente.
 * <p>
 * <b>Ruolo nell'architettura:</b> l'impegnativa è creata/gestita da
 * {@link prenotazione.medica.services.ImpegnativaService} ed esposta da
 * {@link prenotazione.medica.controller.ImpegnativaController}. Può essere collegata a una
 * {@link PrestazioneSanitaria} (uno-a-uno). Regione, codice NRE e priorità sono usati per
 * identificazione e flussi amministrativi.
 * </p>
 *
 * @see EPrioritàPrescrizione – priorità della prescrizione.
 * @see ETipoRichiesta – tipo di ricetta (es. farmaco, visita).
 */
@Entity
@Table(name="impegnativa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Impegnativa {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

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

	@OneToOne(mappedBy = "impegnativa", cascade = CascadeType.ALL)
	private PrestazioneSanitaria prestazioneSanitaria;
}
