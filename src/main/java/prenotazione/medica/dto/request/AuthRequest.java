package prenotazione.medica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Richiesta di login (credenziali).
 * Body di POST {@code /api/auth/login}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 1, max = 50, message = "{validation.size}")
    private String username;

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 1, max = 255, message = "{validation.size}")
    private String password;
}
