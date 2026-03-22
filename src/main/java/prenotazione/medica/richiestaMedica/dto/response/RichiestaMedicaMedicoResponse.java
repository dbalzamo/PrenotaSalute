package prenotazione.medica.richiestaMedica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.richiestaMedica.api.RichiestaMedicaController;
import prenotazione.medica.richiestaMedica.service.RichiestaMedicaService;

import java.util.Date;

/**
 * DTO di una richiesta medica nella vista del medico (lista/dettaglio).
 * <p>
 * Contiene dati della richiesta e dati anagrafici del paziente (id, nome, cognome) per evitare
 * di caricare l'entità Paziente. Restituito da API come GET richieste del medico.
 * </p>
 *
 * @see RichiestaMedicaController
 * @see RichiestaMedicaService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RichiestaMedicaMedicoResponse {
    private Long id;
    private Date dataEmissione;
    private String tipoRichiesta;
    private String stato;
    private String descrizione;
    private Long pazienteId;
    private String pazienteNome;
    private String pazienteCognome;
    /** Id impegnativa generata da questa richiesta, se presente. */
    private Long impegnativaId;
}
