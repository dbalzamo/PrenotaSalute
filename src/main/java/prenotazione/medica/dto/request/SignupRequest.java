package prenotazione.medica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import prenotazione.medica.enums.ERuolo;

import java.util.Date;

/**
 * Richiesta di registrazione di un nuovo utente (paziente o medico curante).
 * <p>
 * Body di POST {@code /api/auth/signup}. Contiene credenziali (username, email, password), ruolo,
 * dati anagrafici comuni e, per il medico, la specializzazione. Opzionalmente il paziente può
 * indicare il medico curante da associare. Validato con Bean Validation prima di essere processato
 * da {@link prenotazione.medica.controller.AuthController} e
 * {@link prenotazione.medica.services.AccountService}.
 * </p>
 */
@Data
public class SignupRequest
{
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(max = 255)
    private String password;
    @NotBlank
    private ERuolo ruolo;

    // Campi comuni
    @NotBlank
    private String nome;
    @NotBlank
    private String cognome;
    @NotBlank
    @Size(max = 16)
    private String codiceFiscale;
    @NotBlank
    private String indirizzoDiResidenza;
    @NotBlank
    private Date dataDiNascita;

    // Campi solo per medico
    private String specializzazione;

    /** Opzionale: id del medico curante da associare al paziente in fase di registrazione. */
    private Long medicoCuranteId;
}