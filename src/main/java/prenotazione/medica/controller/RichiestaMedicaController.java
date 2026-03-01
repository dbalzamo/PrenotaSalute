package prenotazione.medica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import prenotazione.medica.dto.request.RichiestaMedicaRequest;
import prenotazione.medica.dto.request.RifiutoRichiestaRequest;
import prenotazione.medica.enums.EStatoRichiesta;
import prenotazione.medica.services.RichiestaMedicaService;

/**
 * Controller per le richieste mediche: creazione (paziente), elenchi per paziente/medico,
 * visualizzazione, accettazione, rifiuto e filtri per stato.
 * <p>
 * <b>Ruolo nell'architettura:</b> espone /api/richieste-mediche (crea-richiesta, mie-richieste,
 * medico/richieste, trova-richiesta, visualizza, accetta, rifiuta). I metodi sono protetti da
 * @PreAuthorize per ruolo PAZIENTE o MEDICO_CURANTE. Delega tutta la logica a
 * {@link RichiestaMedicaService}.
 * </p>
 */
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

    /** Tutte le richieste del paziente loggato (ordinate per data, più recente prima). */
    @GetMapping("/mie-richieste")
    @PreAuthorize("hasAnyRole('PAZIENTE')")
    public ResponseEntity<?> getMieRichieste()
    {
        return ResponseEntity.ok().body(richiestaMedicaServices.findAllByPazienteId());
    }

    /** Tutte le richieste associate al medico curante loggato (per la dashboard medico). */
    @GetMapping("/medico/richieste")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public ResponseEntity<?> getRichiesteMedico()
    {
        return ResponseEntity.ok().body(richiestaMedicaServices.findAllByMedicoCuranteId());
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

    @PutMapping("/accetta-richiesta/{idRichiestaMedica}")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public ResponseEntity<?> accettaRichiestaMedica(@PathVariable Long idRichiestaMedica)
    {
        richiestaMedicaServices.accettaRichiestaMedica(idRichiestaMedica);
        return ResponseEntity.ok().body("Richiesta accettata.");
    }

    @PostMapping("/rifiuta-richiesta")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public ResponseEntity<?> rifiutaRichiestaMedica(@RequestBody RifiutoRichiestaRequest rifiutoRichiestaRequest)
    {
        return ResponseEntity.ok().body(richiestaMedicaServices.rifiutaRichiestaMedica(rifiutoRichiestaRequest));
    }
}