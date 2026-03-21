package prenotazione.medica.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Richiesta di cambio password per l'account corrente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 6, max = 255, message = "{validation.password.size}")
    private String oldPassword;

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 6, max = 255, message = "{validation.password.size}")
    private String newPassword;
}

