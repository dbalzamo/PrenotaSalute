package prenotazione.medica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import prenotazione.medica.enums.ERuolo;

import java.util.Date;

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
}