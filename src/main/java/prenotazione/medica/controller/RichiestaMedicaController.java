package prenotazione.medica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import prenotazione.medica.dto.request.RichiestaMedicaRequest;
import prenotazione.medica.dto.request.RifiutoRichiestaRequest;
import prenotazione.medica.enums.EStatoRichiesta;
import prenotazione.medica.services.RichiestaMedicaService;

@RestController
@RequestMapping("/api/richieste-mediche")
public class RichiestaMedicaController
{
    @Autowired
    private RichiestaMedicaService richiestaMedicaServices;


    @PostMapping("/crea-richiesta")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    public ResponseEntity<?> creaRichiestaMedica(@RequestBody RichiestaMedicaRequest request)
    {
        return ResponseEntity.ok(richiestaMedicaServices.creaRichiestaMedica(request));
    }

    @GetMapping("/trova-richiesta")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE', 'PAZIENTE')")
    public ResponseEntity<?> getRichiestePerStato(@RequestParam(name = "stato", required = false) EStatoRichiesta stato)
    {
        if (stato == null) // UTILIZZATO DI DEFAULT PER IL MEDICO CURANTE (NOTIFICA NEW REQUEST)
            stato = EStatoRichiesta.INVIATA;

        return ResponseEntity.ok().body(richiestaMedicaServices.findAllByStatoAndPazienteId(stato));
    }

    @PutMapping("/visualizza-richiesta/{idRichiestaMedica}")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public ResponseEntity<?> visualizzaRichiestaMedica(@PathVariable Long idRichiestaMedica)
    {
        return ResponseEntity.ok().body(richiestaMedicaServices.visualizzaRichiestaMedica(idRichiestaMedica));
    }

    @PostMapping("/rifiuta-richiesta")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public ResponseEntity<?> rifiutaRichiestaMedica(@RequestParam RifiutoRichiestaRequest rifiutoRichiestaRequest)
    {
        return ResponseEntity.ok().body(richiestaMedicaServices.rifiutaRichiestaMedica(rifiutoRichiestaRequest));
    }
}