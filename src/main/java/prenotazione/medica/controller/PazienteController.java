package prenotazione.medica.controller;

import com.prenotasalute.commons.controller.GenericController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import prenotazione.medica.dto.PazienteDTO;
import prenotazione.medica.dto.response.MedicoCuranteResponse;
import prenotazione.medica.security.utils.SecurityUtils;
import prenotazione.medica.services.I18nMessageService;
import prenotazione.medica.services.PazienteService;

/**
 * Controller per Paziente: CRUD generico (commons) su /api/v1/pazienti e endpoint specifici
 * per profilo (me), medico curante (mio-medico) e aggiornamento profilo (updatePaziente).
 */
@RestController
@RequestMapping("/api/v1/pazienti")
public class PazienteController extends GenericController<PazienteDTO, Long> {

    private static final Logger logger = LoggerFactory.getLogger(PazienteController.class);

    private final PazienteService pazienteService;
    private final I18nMessageService i18n;

    public PazienteController(PazienteService service, I18nMessageService i18n) {
        super(service);
        this.pazienteService = service;
        this.i18n = i18n;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    public PazienteDTO getCurrentPaziente() {
        return pazienteService.findByAccountIdAsDto(SecurityUtils.getCurrentAccountId());
    }

    @GetMapping("/mio-medico")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    public ResponseEntity<MedicoCuranteResponse> getMioMedicoCurante() {
        try {
            Long accountId = SecurityUtils.getCurrentAccountId();
            MedicoCuranteResponse medico = pazienteService.getMedicoCuranteResponseByPazienteAccountId(accountId);
            if (medico == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(medico);
        } catch (RuntimeException e) {
            logger.warn("getMioMedicoCurante error: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Throwable t) {
            logger.error("getMioMedicoCurante unexpected error", t);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/mio-medico")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
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
    public ResponseEntity<String> updatePaziente(@RequestBody @Valid PazienteDTO pazienteDTO) {
        Long idAccount = SecurityUtils.getCurrentAccountId();
        return ResponseEntity.status(HttpStatus.OK).body(pazienteService.updatePaziente(idAccount, pazienteDTO));
    }
}
