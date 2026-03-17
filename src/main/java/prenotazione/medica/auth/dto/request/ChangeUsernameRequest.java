package prenotazione.medica.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Richiesta di cambio username per l'account corrente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUsernameRequest {

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 3, max = 50, message = "{validation.size.range}")
    private String newUsername;
}

