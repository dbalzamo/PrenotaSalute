package prenotazione.medica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prenotazione.medica.services.MedicoCuranteService;

@RestController
@RequestMapping("/api/medico")
public class MedicoCuranteController
{
    @Autowired
    private MedicoCuranteService medicoCuranteService;


}