package prenotazione.medica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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