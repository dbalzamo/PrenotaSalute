package prenotazione.medica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per CRUD dell'entità MedicoCurante (esposizione e aggiornamento dati anagrafici).
 * Usato dagli endpoint generici /api/v1/medici-curanti.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicoCuranteDTO {
    private Long id;
    private String nome;
    private String cognome;
    private String indirizzoDiResidenza;
    private String dataDiNascita;
    private String codiceFiscale;
    private String specializzazione;
    private String email;
}
