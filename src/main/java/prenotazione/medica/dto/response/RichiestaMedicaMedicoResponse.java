package prenotazione.medica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO di una richiesta medica nella vista del medico (lista/dettaglio).
 * <p>
 * Contiene dati della richiesta e dati anagrafici del paziente (id, nome, cognome) per evitare
 * di caricare l'entità Paziente. Restituito da API come GET richieste del medico.
 * </p>
 *
 * @see prenotazione.medica.controller.RichiestaMedicaController
 * @see prenotazione.medica.services.RichiestaMedicaService
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
}
