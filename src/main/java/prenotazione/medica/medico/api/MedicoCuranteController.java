package prenotazione.medica.medico.api;

import prenotazione.medica.shared.utility.controller.GenericController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prenotazione.medica.medico.dto.MedicoCuranteDTO;
import prenotazione.medica.medico.service.MedicoCuranteService;
import prenotazione.medica.auth.SecurityUtils;

/**
 * Controller per MedicoCurante: CRUD generico (commons) su /api/v1/medici-curanti e endpoint
 * specifici per profilo (me), elenco pazienti (pazienti) e anteprime conversazioni (conversazioni).
 */
@RestController
@RequestMapping("/api/v1/medici-curanti")
@Tag(name = "Medici curanti", description = "Gestione profilo medico curante, pazienti associati e conversazioni.")
public class MedicoCuranteController extends GenericController<MedicoCuranteDTO, Long> {

    private final MedicoCuranteService medicoCuranteService;

    public MedicoCuranteController(MedicoCuranteService service) {
        super(service);
        this.medicoCuranteService = service;
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

}

