package prenotazione.medica.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per prestazione sanitaria (lettura e richiesta impegnativa).
 * In contesto richiesta: codicePrestazione, descrizione, note, quantita sono validati.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestazioneSanitariaDTO {

    private Long id;

    @NotNull(message = "{validation.required}")
    private Long codicePrestazione;

    @Size(max = 255, message = "{validation.size.max}")
    private String descrizione;

    @Size(max = 255, message = "{validation.size.max}")
    private String note;

    @NotNull(message = "{validation.required}")
    @Min(value = 1, message = "{validation.min}")
    private Integer quantita;
}
