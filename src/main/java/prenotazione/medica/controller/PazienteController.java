package prenotazione.medica.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prenotazione.medica.services.PazienteService;

@RestController
@RequestMapping("/api/paziente")
public class PazienteController
{
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private PazienteService pazienteService;

}