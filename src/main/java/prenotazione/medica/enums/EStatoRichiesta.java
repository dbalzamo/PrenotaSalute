package prenotazione.medica.enums;

/**
 * Stati del ciclo di vita di una {@link prenotazione.medica.model.RichiestaMedica}.
 * <p>
 * Usato per filtrare richieste (es. "in attesa" per il medico), notifiche e transizioni
 * (accettata/rifiutata). Il flusso è gestito da
 * {@link prenotazione.medica.services.RichiestaMedicaService}.
 * </p>
 */
public enum EStatoRichiesta
{
    INVIATA,
    VISUALIZZATA,
    ACCETTATA,
    RIFIUTATA,
    ANNULLATA,
    SCADUTA
}