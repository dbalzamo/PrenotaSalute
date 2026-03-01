package prenotazione.medica.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.dto.PrestazioneSanitariaDTO;
import prenotazione.medica.enums.EPrioritàPrescrizione;

/**
 * Richiesta per la creazione/emissione di un'impegnativa a partire da una richiesta medica accettata.
 * <p>
 * Body usato dalle API di emissione impegnativa. Collega la richiesta medica ({@code idRichiestaMedica}),
 * la priorità e i dettagli della prestazione sanitaria. Elaborato da
 * {@link prenotazione.medica.services.ImpegnativaService} e
 * {@link prenotazione.medica.controller.ImpegnativaController}.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImpegnativaRequest
{
    private Long idRichiestaMedica;
    private EPrioritàPrescrizione priorita;
    private PrestazioneSanitariaDTO prestazioneSanitariaDTO;
}