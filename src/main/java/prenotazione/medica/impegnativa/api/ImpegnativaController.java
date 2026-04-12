package prenotazione.medica.impegnativa.api;

import prenotazione.medica.shared.utility.controller.GenericController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('PAZIENTE', 'MEDICO_CURANTE')")
    @Operation(
            summary = "Scarica PDF impegnativa",
            description = "Restituisce il documento PDF dell'impegnativa se il richiedente è il paziente o il medico titolare."
    )
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdf = impegnativaService.getPdfForCurrentUser(id);
        String filename = "impegnativa-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
