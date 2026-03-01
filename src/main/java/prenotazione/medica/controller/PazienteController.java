package prenotazione.medica.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import prenotazione.medica.dto.PazienteDTO;
import prenotazione.medica.dto.response.MedicoCuranteResponse;
import prenotazione.medica.model.MedicoCurante;
import prenotazione.medica.model.Paziente;
import prenotazione.medica.security.utils.SecurityUtils;
import prenotazione.medica.services.PazienteService;

/**
 * Controller per le API riservate al paziente: profilo, medico curante (GET/PUT), area personale.
 * <p>
 * <b>Ruolo nell'architettura:</b> GET /api/paziente/me, /mio-medico, PUT /api/paziente/mio-medico.
 * getMioMedicoCurante e putMioMedicoCurante sono usati dalla Posta e dall'area personale per
 * associare o leggere il medico curante. L'id account corrente è ottenuto con SecurityUtils.
 * </p>
 */
@RestController
@RequestMapping("/api/paziente")
public class PazienteController
{
    private static final Logger logger = LoggerFactory.getLogger(PazienteController.class);

    @Autowired
    private PazienteService pazienteService;

    /** Restituisce il paziente associato all'account attualmente autenticato (per profilo). */
    @GetMapping("/me")
    public Paziente getCurrentPaziente() {
        return pazienteService.findByAccountId(SecurityUtils.getCurrentAccountId());
    }

    /**
     * Restituisce il medico curante associato al paziente loggato (per Posta / messaggistica).
     * 404 se il paziente non ha un medico assegnato o l'account non è un paziente.
     */
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

    /**
     * Associa il medico curante al paziente loggato. Body: { "medicoCuranteId": number }
     */
    @PutMapping("/mio-medico")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    public ResponseEntity<String> setMioMedicoCurante(@RequestBody java.util.Map<String, Long> body) {
        Long medicoCuranteId = body != null ? body.get("medicoCuranteId") : null;
        if (medicoCuranteId == null) {
            return ResponseEntity.badRequest().body("medicoCuranteId obbligatorio");
        }
        pazienteService.setMedicoCurante(SecurityUtils.getCurrentAccountId(), medicoCuranteId);
        return ResponseEntity.ok("Medico curante associato.");
    }

    @PutMapping("/updatePaziente")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    public ResponseEntity<String> updatePaziente(@RequestBody PazienteDTO pazienteDTO) {
        Long idAccount = SecurityUtils.getCurrentAccountId();
        return ResponseEntity.status(HttpStatus.OK).body(pazienteService.updatePaziente(idAccount, pazienteDTO));
    }
}