package prenotazione.medica.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.dto.PrestazioneSanitariaDTO;
import prenotazione.medica.enums.EPriorità;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImpegnativaRequest
{
    private Long idRichiestaMedica;
    private EPriorità priorita;
    private PrestazioneSanitariaDTO prestazioneSanitariaDTO;
}