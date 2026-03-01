package prenotazione.medica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prenotazione.medica.dto.ConversazionePreviewDTO;
import prenotazione.medica.dto.PazientePerMessaggioDTO;
import prenotazione.medica.model.MedicoCurante;
import prenotazione.medica.security.utils.SecurityUtils;
import prenotazione.medica.services.MedicoCuranteService;
import prenotazione.medica.services.MessageService;

import java.util.List;

/**
 * Controller per le API riservate al medico curante: profilo, elenco pazienti, anteprime conversazioni.
 * <p>
 * <b>Ruolo nell'architettura:</b> GET /api/medico/me, /pazienti, /conversazioni. Tutti i metodi
 * richiedono ruolo MEDICO_CURANTE (@PreAuthorize). L'id account corrente è ottenuto con
 * {@link SecurityUtils#getCurrentAccountId}. La lista chat (anteprime) è fornita da
 * {@link MessageService#getConversationPreviewsForMedico}.
 * </p>
 */
@RestController
@RequestMapping("/api/medico")
public class MedicoCuranteController
{
    @Autowired
    private MedicoCuranteService medicoCuranteService;
    @Autowired
    private MessageService messageService;

    /** Restituisce il medico curante associato all'account autenticato (per area personale). */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public MedicoCurante getCurrentMedicoCurante() {
        return medicoCuranteService.findByAccountId(SecurityUtils.getCurrentAccountId());
    }

    /** Elenco pazienti del medico (per messaggistica). */
    @GetMapping("/pazienti")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public List<PazientePerMessaggioDTO> getPazienti() {
        return medicoCuranteService.findPazientiForMessaging(SecurityUtils.getCurrentAccountId());
    }

    /** Anteprima conversazioni: pazienti con ultimo messaggio, orario e numero non letti (lista chat stile WhatsApp). */
    @GetMapping("/conversazioni")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public List<ConversazionePreviewDTO> getConversazioni() {
        return messageService.getConversationPreviewsForMedico(SecurityUtils.getCurrentAccountId());
    }
}