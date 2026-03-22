package prenotazione.medica.richiestaMedica.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO compatto per la lista richieste mediche lato paziente/medico.
 * Contiene solo i campi di riepilogo necessari alla lista (id, date, tipo, stato, descrizione).
 */
@Data
@NoArgsConstructor
public class RichiestaMedicaListItemDTO {
    private Long id;
    private Date dataEmissione;
    private Date dataAccettazione;
    private String tipoRichiesta;
    private String stato;
    private String descrizione;
    /** Popolato quando è stata emessa un'impegnativa collegata alla richiesta. */
    private Long impegnativaId;
}

