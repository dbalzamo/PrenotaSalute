package prenotazione.medica.impegnativa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.shared.enums.EPrioritàPrescrizione;
import prenotazione.medica.shared.enums.ETipoRichiesta;

/**
 * DTO per CRUD dell'entità Impegnativa.
 * Gli id paziente e medico curante sono usati per risolvere le relazioni in create/update.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImpegnativaDTO {
    private Long id;
    private String regione;
    private String codiceNRE;
    private ETipoRichiesta tipoRicetta;
    private EPrioritàPrescrizione priorita;
    private Long idPaziente;
    private Long idMedicoCurante;
}
