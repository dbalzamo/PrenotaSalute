package prenotazione.medica.prestazioneSanitaria.api;

import prenotazione.medica.shared.utility.controller.GenericController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prenotazione.medica.prestazioneSanitaria.dto.PrestazioneSanitariaDTO;
import prenotazione.medica.prestazioneSanitaria.service.PrestazioneSanitariaService;

/**
 * Controller per PrestazioneSanitaria: CRUD generico (commons) su /api/v1/prestazioni-sanitarie.
 */
@RestController
@RequestMapping("/api/v1/prestazioni-sanitarie")
@Tag(name = "Prestazioni sanitarie", description = "Gestione del catalogo delle prestazioni sanitarie.")
public class PrestazioneSanitariaController extends GenericController<PrestazioneSanitariaDTO, Long> {

    public PrestazioneSanitariaController(PrestazioneSanitariaService service) {
        super(service);
    }
}
