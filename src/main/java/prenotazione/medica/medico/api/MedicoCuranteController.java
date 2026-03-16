package prenotazione.medica.medico.api;

import com.prenotasalute.commons.controller.GenericController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prenotazione.medica.chat.dto.ConversazionePreviewDTO;
import prenotazione.medica.medico.dto.MedicoCuranteDTO;
import prenotazione.medica.medico.service.MedicoCuranteService;
import prenotazione.medica.chat.dto.PazientePerMessaggioDTO;
import prenotazione.medica.auth.SecurityUtils;
import prenotazione.medica.chat.service.MessageService;

import java.util.List;

/**
 * Controller per MedicoCurante: CRUD generico (commons) su /api/v1/medici-curanti e endpoint
 * specifici per profilo (me), elenco pazienti (pazienti) e anteprime conversazioni (conversazioni).
 */
@RestController
@RequestMapping("/api/v1/medici-curanti")
public class MedicoCuranteController extends GenericController<MedicoCuranteDTO, Long> {

    private final MedicoCuranteService medicoCuranteService;
    private final MessageService messageService;

    public MedicoCuranteController(MedicoCuranteService service, MessageService messageService) {
        super(service);
        this.medicoCuranteService = service;
        this.messageService = messageService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public MedicoCuranteDTO getCurrentMedicoCurante() {
        return medicoCuranteService.findByAccountIdAsDto(SecurityUtils.getCurrentAccountId());
    }

    @GetMapping("/pazienti")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public List<PazientePerMessaggioDTO> getPazienti() {
        return medicoCuranteService.findPazientiForMessaging(SecurityUtils.getCurrentAccountId());
    }

    @GetMapping("/conversazioni")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public List<ConversazionePreviewDTO> getConversazioni() {
        return messageService.getConversationPreviewsForMedico(SecurityUtils.getCurrentAccountId());
    }
}

