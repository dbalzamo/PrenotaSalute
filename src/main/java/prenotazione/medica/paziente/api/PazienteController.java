package prenotazione.medica.paziente.api;

import prenotazione.medica.shared.utility.controller.GenericController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import prenotazione.medica.medico.dto.response.MedicoCuranteResponse;
import prenotazione.medica.auth.SecurityUtils;
import prenotazione.medica.paziente.dto.PazienteDTO;
import prenotazione.medica.paziente.service.PazienteService;
import prenotazione.medica.shared.i18n.I18nMessageService;

/**
 * Controller per Paziente: CRUD generico (commons) su /api/v1/pazienti e endpoint specifici
 * per profilo (me), medico curante (mio-medico) e aggiornamento profilo (updatePaziente).
 */
@RestController
@RequestMapping("/api/v1/pazienti")
@Tag(name = "Pazienti", description = "Gestione anagrafica e profilo paziente.")
public class PazienteController extends GenericController<PazienteDTO, Long> {

    private final PazienteService pazienteService;
    private final I18nMessageService i18n;

    public PazienteController(PazienteService service, I18nMessageService i18n) {
        super(service);
        this.pazienteService = service;
        this.i18n = i18n;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    @Operation(
            summary = "Profilo paziente corrente",
            description = "Restituisce i dati anagrafici del paziente associato all'account autenticato."
    )
    public PazienteDTO getCurrentPaziente() {
        return pazienteService.findByAccountIdAsDto(SecurityUtils.getCurrentAccountId());
    }

    @GetMapping("/mio-medico")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    @Operation(
            summary = "Medico curante del paziente",
            description = "Restituisce le informazioni sul medico curante associato al paziente corrente. "
                    + "Se non è associato nessun medico, la risposta è 200 con corpo JSON null (evita 404 strumentali in console)."
    )
    public ResponseEntity<MedicoCuranteResponse> getMioMedicoCurante() {
        Long accountId = SecurityUtils.getCurrentAccountId();
        MedicoCuranteResponse medico = pazienteService.getMedicoCuranteResponseByPazienteAccountId(accountId);
        return ResponseEntity.ok(medico);
    }

    @PutMapping("/mio-medico")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    @Operation(
            summary = "Imposta medico curante",
            description = "Associa un medico curante al paziente corrente a partire dall'identificativo medicoCuranteId."
    )
    public ResponseEntity<String> setMioMedicoCurante(@RequestBody java.util.Map<String, Long> body) {
        Long medicoCuranteId = body != null ? body.get("medicoCuranteId") : null;
        if (medicoCuranteId == null) {
            return ResponseEntity.badRequest().body(i18n.getMessage("paziente.medico.required"));
        }
        pazienteService.setMedicoCurante(SecurityUtils.getCurrentAccountId(), medicoCuranteId);
        return ResponseEntity.ok(i18n.getMessage("paziente.medico.associated"));
    }

    @PutMapping("/updatePaziente")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    @Operation(
            summary = "Aggiorna profilo paziente",
            description = "Aggiorna i dati anagrafici del paziente corrente con le informazioni fornite nel DTO."
    )
    public ResponseEntity<String> updatePaziente(@RequestBody @Valid PazienteDTO pazienteDTO) {
        Long idAccount = SecurityUtils.getCurrentAccountId();
        return ResponseEntity.status(HttpStatus.OK).body(pazienteService.updatePaziente(idAccount, pazienteDTO));
    }
}

