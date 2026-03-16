package prenotazione.medica.medico.api;

import com.prenotasalute.commons.controller.GenericController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Medici curanti", description = "Gestione profilo medico curante, pazienti associati e conversazioni.")
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
    @Operation(
            summary = "Profilo medico curante corrente",
            description = "Restituisce i dati del medico curante associato all'account autenticato."
    )
    public MedicoCuranteDTO getCurrentMedicoCurante() {
        return medicoCuranteService.findByAccountIdAsDto(SecurityUtils.getCurrentAccountId());
    }

    @GetMapping("/pazienti")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    @Operation(
            summary = "Elenco pazienti del medico",
            description = "Restituisce i pazienti associati al medico curante corrente, per la messaggistica."
    )
    public List<PazientePerMessaggioDTO> getPazienti() {
        return medicoCuranteService.findPazientiForMessaging(SecurityUtils.getCurrentAccountId());
    }

    @GetMapping("/conversazioni")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    @Operation(
            summary = "Conversazioni del medico",
            description = "Restituisce le anteprime delle conversazioni attive del medico curante corrente."
    )
    public List<ConversazionePreviewDTO> getConversazioni() {
        return messageService.getConversationPreviewsForMedico(SecurityUtils.getCurrentAccountId());
    }
}

