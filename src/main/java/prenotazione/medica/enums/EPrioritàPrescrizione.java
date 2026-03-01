package prenotazione.medica.enums;

/**
 * Priorità di una prescrizione ({@link prenotazione.medica.model.Impegnativa}).
 * <p>
 * Usato per ordinare o evidenziare impegnative (urgente, breve, differibile, programmata) nelle
 * API e nella dashboard medico.
 * </p>
 */
public enum EPrioritàPrescrizione
{
    URGENTE,
    BREVE,
    DIFFERIBILE,
    PROGRAMMATA
}