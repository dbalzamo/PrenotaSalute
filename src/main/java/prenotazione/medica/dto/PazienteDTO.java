package prenotazione.medica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per l'esposizione e aggiornamento dei dati anagrafici di un paziente.
 * <p>
 * Usato dalle API che restituiscono il profilo del paziente loggato e in PUT updatePaziente.
 * La validazione si applica quando il DTO è usato come body di richiesta ({@code @Valid}).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PazienteDTO {

    private Long id;

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 1, max = 100, message = "{validation.size.range}")
    private String nome;

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 1, max = 100, message = "{validation.size.range}")
    private String cognome;

    @NotBlank(message = "{validation.notblank}")
    @Email(message = "{validation.email}")
    @Size(max = 100, message = "{validation.size.max}")
    private String email;

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 16, max = 16, message = "{validation.codicefiscale.size}")
    private String codiceFiscale;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 200, message = "{validation.size.max}")
    private String indirizzoDiResidenza;

    @Size(max = 30, message = "{validation.size.max}")
    private String dataDiNascita;
}