package prenotazione.medica.dto.request;

import lombok.Data;

/**
 * Richiesta di creazione di una richiesta medica da parte del paziente.
 * <p>
 * Body di POST per inviare una nuova richiesta al medico (tipo, descrizione, id medico destinatario).
 * Usato da {@link prenotazione.medica.controller.RichiestaMedicaController} e
 * {@link prenotazione.medica.services.RichiestaMedicaService}.
 * </p>
 */
@Data
public class RichiestaMedicaRequest
{
    private String tipoRichiesta;
    private String descrizione;
    private Long idMedico;
}