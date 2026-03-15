package prenotazione.medica.controller;

import com.prenotasalute.commons.controller.GenericController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import prenotazione.medica.dto.RichiestaMedicaDTO;
import prenotazione.medica.dto.request.RichiestaMedicaRequest;
import prenotazione.medica.dto.request.RifiutoRichiestaRequest;
import prenotazione.medica.enums.EStatoRichiesta;
import prenotazione.medica.services.I18nMessageService;
import prenotazione.medica.services.RichiestaMedicaService;

/**
 * Controller per RichiestaMedica: CRUD generico (commons) su /api/v1/richieste-mediche e endpoint
 * specifici per creazione (crea-richiesta), elenchi (mie-richieste, medico/richieste),
 * filtri (trova-richiesta), visualizza, accetta, rifiuta.
 */
@RestController
@RequestMapping("/api/v1/richieste-mediche")
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
    public ResponseEntity<?> creaRichiestaMedica(@RequestBody @Valid RichiestaMedicaRequest request) {
        return ResponseEntity.ok(richiestaMedicaService.creaRichiestaMedica(request));
    }

    @GetMapping("/mie-richieste")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    public ResponseEntity<?> getMieRichieste() {
        return ResponseEntity.ok().body(richiestaMedicaService.findAllByPazienteId());
    }

    @GetMapping("/medico/richieste")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public ResponseEntity<?> getRichiesteMedico() {
        return ResponseEntity.ok().body(richiestaMedicaService.findAllByMedicoCuranteId());
    }

    @GetMapping("/trova-richiesta")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE', 'PAZIENTE')")
    public ResponseEntity<?> getRichiestePerStato(@RequestParam(name = "stato", required = false) EStatoRichiesta stato) {
        if (stato == null) {
            stato = EStatoRichiesta.INVIATA;
        }
        return ResponseEntity.ok().body(richiestaMedicaService.findAllByStatoAndPazienteId(stato));
    }

    @PutMapping("/visualizza-richiesta/{idRichiestaMedica}")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public ResponseEntity<?> visualizzaRichiestaMedica(@PathVariable Long idRichiestaMedica) {
        return ResponseEntity.ok().body(richiestaMedicaService.visualizzaRichiestaMedica(idRichiestaMedica));
    }

    @PutMapping("/accetta-richiesta/{idRichiestaMedica}")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public ResponseEntity<?> accettaRichiestaMedica(@PathVariable Long idRichiestaMedica) {
        richiestaMedicaService.accettaRichiestaMedica(idRichiestaMedica);
        return ResponseEntity.ok().body(i18n.getMessage("richiesta.accepted"));
    }

    @PostMapping("/rifiuta-richiesta")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public ResponseEntity<?> rifiutaRichiestaMedica(@RequestBody @Valid RifiutoRichiestaRequest rifiutoRichiestaRequest) {
        return ResponseEntity.ok().body(richiestaMedicaService.rifiutaRichiestaMedica(rifiutoRichiestaRequest));
    }
}
