package prenotazione.medica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per una voce dell'elenco pazienti del medico (messaggistica e selezione conversazione).
 * <p>
 * Restituito da GET {@code /api/medico/pazienti}. Usato per popolare la lista chat e per passare
 * l'{@code accountId} necessario a inviare messaggi (WebSocket/REST) e a caricare la conversazione.
 * </p>
 *
 * @see prenotazione.medica.services.MedicoCuranteService#findPazientiForMessaging
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PazientePerMessaggioDTO {
    private Long id;
    private String nome;
    private String cognome;
    /** ID account per invio messaggi (WebSocket e REST). */
    private Long accountId;
}
