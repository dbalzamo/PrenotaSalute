package prenotazione.medica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per l'esposizione dei dati anagrafici di un paziente (es. profilo, area personale).
 * <p>
 * Usato dalle API che restituiscono il profilo del paziente loggato o dati paziente in contesti
 * dove non serve l'entità completa con relazioni (richieste, impegnative, account).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PazienteDTO
{
    private Long id;
    private String nome;
    private String cognome;
    private String email;
    private String codiceFiscale;
    private String indirizzoDiResidenza;
    private String dataDiNascita;
}