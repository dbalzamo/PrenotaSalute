package prenotazione.medica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestazioneSanitariaDTO
{
    private Long codicePrestazione;
    private String descrizione;
    private String note;
    private int quantita;
}