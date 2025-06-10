package prenotazione.medica.dto.request;

import lombok.Data;

@Data
public class RichiestaMedicaRequest
{
    private String tipoRichiesta;
    private String descrizione;
    private Long idMedico;
}