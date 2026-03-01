package prenotazione.medica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Richiesta di rifiuto di una richiesta medica da parte del medico.
 * <p>
 * Body delle API di rifiuto: id della richiesta e motivazione obbligatoria. Validato e processato
 * da {@link prenotazione.medica.controller.RichiestaMedicaController} e
 * {@link prenotazione.medica.services.RichiestaMedicaService}.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RifiutoRichiestaRequest
{
    @NotNull
    private Long idRichiesta;

    @NotBlank
    private String motivazione;
}