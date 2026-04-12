package prenotazione.medica.shared.utility.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import prenotazione.medica.shared.utility.service.AbstractGenericService;

import java.io.Serializable;
import java.util.List;

/**
 * Endpoint CRUD generici documentati in OpenAPI; il tag del dominio resta sul controller concreto
 * (es. Pazienti, Impegnative).
 */
@SecurityRequirement(name = "bearer-jwt")
public abstract class GenericController<DTO, ID extends Serializable> {

    protected final AbstractGenericService<?, DTO, ID> service;

    protected GenericController(AbstractGenericService<?, DTO, ID> service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio per id", description = "Restituisce una singola risorsa in base all'identificativo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Risorsa trovata",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Risorsa non trovata", content = @Content)
    })
    public ResponseEntity<DTO> getById(
            @Parameter(description = "Identificativo primario della risorsa", required = true)
            @PathVariable ID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/getAll")
    @Operation(summary = "Elenco completo", description = "Restituisce tutte le risorse (senza paginazione; usare con cautela su dataset grandi).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elenco restituito",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<List<DTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/getAll/paged")
    @Operation(
            summary = "Elenco paginato",
            description = "Restituisce una pagina Spring Data (`Page`). Parametri standard: `page`, `size`, `sort`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagina restituita",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<Page<DTO>> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @PostMapping
    @Operation(summary = "Crea risorsa", description = "Crea una nuova risorsa; il corpo è il DTO della risorsa.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Risorsa creata",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Dati non validi", content = @Content)
    })
    public ResponseEntity<DTO> create(@RequestBody DTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna risorsa", description = "Aggiorna la risorsa esistente con l'id indicato.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Risorsa aggiornata",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Risorsa non trovata", content = @Content)
    })
    public ResponseEntity<DTO> update(
            @Parameter(description = "Identificativo primario della risorsa", required = true)
            @PathVariable ID id,
            @RequestBody DTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina risorsa", description = "Rimuove la risorsa con l'id indicato.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminazione riuscita", content = @Content),
            @ApiResponse(responseCode = "404", description = "Risorsa non trovata", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificativo primario della risorsa", required = true)
            @PathVariable ID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
