package prenotazione.medica.enums;

/**
 * Tipologia di una richiesta medica o di un'impegnativa.
 * <p>
 * Usato in {@link prenotazione.medica.model.RichiestaMedica#tipoRichiesta} e
 * {@link prenotazione.medica.model.Impegnativa#tipoRicetta}. Consente filtri e statistiche
 * per tipo (visita, esame, prescrizione, ecc.).
 * </p>
 */
public enum ETipoRichiesta
{
    VISITA,
    PRESCRIZIONE,
    ESAME,
    CONTROLLO_REFERTI,
    ALTRO
}