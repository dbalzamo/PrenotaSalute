package prenotazione.medica.richiestaMedica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.shared.enums.EStatoRichiesta;
import prenotazione.medica.shared.enums.ETipoRichiesta;

import java.util.Date;

/**
 * DTO per CRUD dell'entità RichiestaMedica.
 * Gli id paziente e medico curante sono usati per risolvere le relazioni in create/update.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RichiestaMedicaDTO {
    private Long id;
    private Date dataEmissione;
    private Date dataAccettazione;
    private ETipoRichiesta tipoRichiesta;
    private EStatoRichiesta stato;
    private String descrizione;
    private Long idPaziente;
    private Long idMedicoCurante;
}
