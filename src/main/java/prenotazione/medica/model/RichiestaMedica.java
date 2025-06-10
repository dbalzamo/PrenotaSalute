package prenotazione.medica.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.enums.EStatoRichiesta;
import prenotazione.medica.enums.ETipoRichiesta;

@Entity
@Table(name="richiesta_medica")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RichiestaMedica
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "data_richiesta", nullable = false)
	private Date dataEmissione;

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