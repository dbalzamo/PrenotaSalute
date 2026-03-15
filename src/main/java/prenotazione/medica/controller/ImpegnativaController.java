package prenotazione.medica.controller;

import com.prenotasalute.commons.controller.GenericController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import prenotazione.medica.dto.ImpegnativaDTO;
import prenotazione.medica.dto.request.ImpegnativaRequest;
import prenotazione.medica.services.ImpegnativaService;

/**
 * Controller per Impegnativa: CRUD generico (commons) su /api/v1/impegnative e endpoint
 * specifico per generazione da richiesta medica (POST /genera-impegnativa).
 * Solo MEDICO_CURANTE può generare impegnative.
 */
@RestController
@RequestMapping("/api/v1/impegnative")
public class ImpegnativaController extends GenericController<ImpegnativaDTO, Long> {

    private final ImpegnativaService impegnativaService;

    public ImpegnativaController(ImpegnativaService service) {
        super(service);
        this.impegnativaService = service;
    }

    @PostMapping("/genera-impegnativa")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public ResponseEntity<String> generaImpegnativa(@RequestBody @Valid ImpegnativaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(impegnativaService.generaImpegnativa(request));
    }
}
