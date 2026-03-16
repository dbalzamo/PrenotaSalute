package prenotazione.medica.medico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Voce per l'elenco medici curanti (es. selezione in signup del paziente).
 * <p>
 * Usato dalle API pubbliche o da quelle di registrazione per mostrare id, nome e cognome dei
 * medici tra cui il paziente può scegliere il proprio medico curante.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicoCuranteListItemDTO {
    private Long id;
    private String nome;
    private String cognome;
}

