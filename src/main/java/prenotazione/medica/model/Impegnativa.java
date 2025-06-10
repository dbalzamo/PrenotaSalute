package prenotazione.medica.model;

import java.util.Date;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.enums.EPriorità;
import prenotazione.medica.enums.ETipoRichiesta;

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
	private char codiceNRE;

	@Column(name = "data_prescrizione", nullable = false)
	private Date dataPrescrizione;

	@Column(name = "tipo_ricetta", nullable = false, length = 50)
	private ETipoRichiesta tipoRicetta;

	@Enumerated(EnumType.STRING)
	@Column(name = "priorita")
	private EPriorità priorità;

	@ManyToOne
	@JoinColumn(name = "id_paziente")
	private Paziente paziente;
	
	@ManyToOne
	@JoinColumn(name = "id_medico_curante")
	private MedicoCurante medicoCurante;

	@OneToOne(mappedBy = "impegnativa", cascade = CascadeType.ALL)
	private PrestazioneSanitaria prestazioneSanitaria;
}
