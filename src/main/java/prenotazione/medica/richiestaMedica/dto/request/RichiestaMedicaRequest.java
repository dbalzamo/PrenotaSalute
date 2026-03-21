package prenotazione.medica.richiestaMedica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Richiesta di creazione di una richiesta medica (paziente → medico).
 * Body di POST crea-richiesta. Valori tipoRichiesta: VISITA, PRESCRIZIONE, ESAME, CONTROLLO_REFERTI, ALTRO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RichiestaMedicaRequest {

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 50, message = "{validation.size.max}")
    private String tipoRichiesta;

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 1, max = 1000, message = "{validation.size.range}")
    private String descrizione;

    @NotNull(message = "{validation.required}")
    private Long idMedico;
}
