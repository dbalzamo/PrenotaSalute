package prenotazione.medica.impegnativa.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.prestazioneSanitaria.dto.PrestazioneSanitariaDTO;
import prenotazione.medica.shared.enums.EPrioritàPrescrizione;

/**
 * Richiesta per l'emissione di un'impegnativa da richiesta medica accettata.
 * Body usato dalle API di emissione impegnativa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImpegnativaRequest {

    @NotNull(message = "{validation.required}")
    private Long idRichiestaMedica;

    @NotNull(message = "{validation.required}")
    private EPrioritàPrescrizione priorita;

    @NotNull(message = "{validation.required}")
    @Valid
    private PrestazioneSanitariaDTO prestazioneSanitariaDTO;
}
