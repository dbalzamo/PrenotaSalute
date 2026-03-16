package prenotazione.medica.richiestaMedica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Richiesta di rifiuto di una richiesta medica (medico).
 * Body delle API di rifiuto: id richiesta e motivazione obbligatoria.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RifiutoRichiestaRequest {

    @NotNull(message = "{validation.required}")
    private Long idRichiesta;

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 1, max = 500, message = "{validation.size.range}")
    private String motivazione;
}
