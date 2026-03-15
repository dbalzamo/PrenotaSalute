package prenotazione.medica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Risposta per GET {@code /api/v1/pazienti/mio-medico}: dati del medico curante associato al paziente.
 * <p>
 * Espone id, nome, cognome e un blocco {@link AccountInfo} con id account, username ed email, così
 * il frontend può usare l'id account per messaggistica (WebSocket/REST) senza caricare l'entità
 * MedicoCurante con lazy loading che causerebbe errori di serializzazione JSON.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicoCuranteResponse {
    private Long id;
    private String nome;
    private String cognome;
    /** Dati account del medico (id utile per invio messaggi). */
    private AccountInfo account;

    /** Dati essenziali dell'account per evitare lazy load sulla entità Account. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountInfo {
        private Long id;
        private String username;
        private String email;
    }
}
