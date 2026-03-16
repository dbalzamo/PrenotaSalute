package prenotazione.medica.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prenotazione.medica.shared.enums.ERuolo;

import java.util.Date;

/**
 * Richiesta di registrazione (paziente o medico curante).
 * Body di POST {@code /api/auth/signup}. Validato con Bean Validation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 3, max = 50, message = "{validation.size.range}")
    private String username;

    @NotBlank(message = "{validation.notblank}")
    @Email(message = "{validation.email}")
    @Size(max = 50, message = "{validation.size.max}")
    private String email;

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 6, max = 255, message = "{validation.password.size}")
    private String password;

    @NotNull(message = "{validation.required}")
    private ERuolo ruolo;

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 1, max = 20, message = "{validation.size.range}")
    private String nome;

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 1, max = 30, message = "{validation.size.range}")
    private String cognome;

    @NotBlank(message = "{validation.notblank}")
    @Size(min = 16, max = 16, message = "{validation.codicefiscale.size}")
    private String codiceFiscale;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 100, message = "{validation.size.max}")
    private String indirizzoDiResidenza;

    @NotNull(message = "{validation.required}")
    private Date dataDiNascita;

    /** Solo per medico curante. */
    @Size(max = 100, message = "{validation.size.max}")
    private String specializzazione;

    /** Solo per paziente: id medico curante da associare. */
    private Long medicoCuranteId;
}
