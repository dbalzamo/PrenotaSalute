package prenotazione.medica.controller;

import com.prenotasalute.commons.controller.GenericController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prenotazione.medica.dto.PrestazioneSanitariaDTO;
import prenotazione.medica.services.PrestazioneSanitariaService;

/**
 * Controller per PrestazioneSanitaria: CRUD generico (commons) su /api/v1/prestazioni-sanitarie.
 */
@RestController
@RequestMapping("/api/v1/prestazioni-sanitarie")
public class PrestazioneSanitariaController extends GenericController<PrestazioneSanitariaDTO, Long> {

    public PrestazioneSanitariaController(PrestazioneSanitariaService service) {
        super(service);
    }
}
