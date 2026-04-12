package prenotazione.medica.richiestaMedica.api;

import prenotazione.medica.shared.utility.controller.GenericController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import prenotazione.medica.richiestaMedica.service.RichiestaMedicaService;
import prenotazione.medica.richiestaMedica.dto.RichiestaMedicaDTO;
import prenotazione.medica.richiestaMedica.dto.request.RichiestaMedicaRequest;
import prenotazione.medica.richiestaMedica.dto.request.RifiutoRichiestaRequest;
import prenotazione.medica.richiestaMedica.dto.response.RichiestaMedicaListItemDTO;
import prenotazione.medica.shared.enums.EStatoRichiesta;
import prenotazione.medica.shared.i18n.I18nMessageService;

/**
 * Controller per RichiestaMedica: CRUD generico (commons) su /api/v1/richieste-mediche e endpoint
 * specifici per creazione (crea-richiesta), elenchi (mie-richieste, medico/richieste),
 * filtri (trova-richiesta), visualizza, accetta, rifiuta.
 */
@RestController
@RequestMapping("/api/v1/richieste-mediche")
@Tag(name = "Richieste mediche", description = "Gestione delle richieste mediche tra paziente e medico curante.")
public class RichiestaMedicaController extends GenericController<RichiestaMedicaDTO, Long> {

    private final RichiestaMedicaService richiestaMedicaService;
    private final I18nMessageService i18n;

    public RichiestaMedicaController(RichiestaMedicaService service, I18nMessageService i18n) {
        super(service);
        this.richiestaMedicaService = service;
        this.i18n = i18n;
    }

    @PostMapping("/crea-richiesta")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    @Operation(
            summary = "Crea una nuova richiesta medica",
            description = "Permette al paziente autenticato di creare una nuova richiesta medica verso il proprio medico curante."
    )
    public ResponseEntity<?> creaRichiestaMedica(@RequestBody @Valid RichiestaMedicaRequest request) {
        return ResponseEntity.ok(richiestaMedicaService.creaRichiestaMedica(request));
    }

    @GetMapping("/mie-richieste")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    @Operation(
            summary = "Richieste mediche del paziente",
            description = "Restituisce tutte le richieste mediche create dal paziente corrente."
    )
    public ResponseEntity<Page<RichiestaMedicaListItemDTO>> getMieRichieste(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(richiestaMedicaService.findAllByPazienteId(page, size));
    }

    @GetMapping("/medico/richieste")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    @Operation(
            summary = "Richieste mediche per il medico",
            description = "Restituisce le richieste mediche assegnate al medico curante corrente."
    )
    public ResponseEntity<?> getRichiesteMedico() {
        return ResponseEntity.ok().body(richiestaMedicaService.findAllByMedicoCuranteId());
    }

    @GetMapping("/trova-richiesta")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE', 'PAZIENTE')")
    @Operation(
            summary = "Filtra richieste per stato",
            description = "Restituisce le richieste mediche del soggetto corrente filtrate per stato (default INVIATA)."
    )
    public ResponseEntity<Page<RichiestaMedicaListItemDTO>> getRichiestePerStato(
            @RequestParam(name = "stato", required = false) EStatoRichiesta stato,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (stato == null) {
            stato = EStatoRichiesta.INVIATA;
        }
        return ResponseEntity.ok(richiestaMedicaService.findAllByStatoAndPazienteId(stato, page, size));
    }

    @PutMapping("/visualizza-richiesta/{idRichiestaMedica}")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    @Operation(
            summary = "Segna richiesta come visualizzata",
            description = "Segna la richiesta indicata come visualizzata dal medico curante."
    )
    public ResponseEntity<?> visualizzaRichiestaMedica(@PathVariable Long idRichiestaMedica) {
        return ResponseEntity.ok().body(richiestaMedicaService.visualizzaRichiestaMedica(idRichiestaMedica));
    }

    @PutMapping("/accetta-richiesta/{idRichiestaMedica}")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    @Operation(
            summary = "Accetta una richiesta medica",
            description = "Permette al medico curante di accettare la richiesta medica indicata."
    )
    public ResponseEntity<?> accettaRichiestaMedica(@PathVariable Long idRichiestaMedica) {
        richiestaMedicaService.accettaRichiestaMedica(idRichiestaMedica);
        return ResponseEntity.ok().body(i18n.getMessage("richiesta.accepted"));
    }

    @PostMapping("/rifiuta-richiesta")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    @Operation(
            summary = "Rifiuta una richiesta medica",
            description = "Permette al medico curante di rifiutare una richiesta specificando il motivo."
    )
    public ResponseEntity<?> rifiutaRichiestaMedica(@RequestBody @Valid RifiutoRichiestaRequest rifiutoRichiestaRequest) {
        return ResponseEntity.ok().body(richiestaMedicaService.rifiutaRichiestaMedica(rifiutoRichiestaRequest));
    }
}
