package prenotazione.medica.impegnativa.api;

import com.prenotasalute.commons.controller.GenericController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import prenotazione.medica.impegnativa.dto.ImpegnativaDTO;
import prenotazione.medica.impegnativa.dto.request.ImpegnativaRequest;
import prenotazione.medica.impegnativa.service.ImpegnativaService;

/**
 * Controller per Impegnativa: CRUD generico (commons) su /api/v1/impegnative e endpoint
 * specifico per generazione da richiesta medica (POST /genera-impegnativa).
 * Solo MEDICO_CURANTE può generare impegnative.
 */
@RestController
@RequestMapping("/api/v1/impegnative")
@Tag(name = "Impegnative", description = "Gestione delle impegnative emesse dai medici curanti.")
public class ImpegnativaController extends GenericController<ImpegnativaDTO, Long> {

    private final ImpegnativaService impegnativaService;

    public ImpegnativaController(ImpegnativaService service) {
        super(service);
        this.impegnativaService = service;
    }

    @PostMapping("/genera-impegnativa")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    @Operation(
            summary = "Genera una nuova impegnativa",
            description = "Genera una nuova impegnativa a partire dai dati della richiesta medica indicata."
    )
    public ResponseEntity<String> generaImpegnativa(@RequestBody @Valid ImpegnativaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(impegnativaService.generaImpegnativa(request));
    }
}
