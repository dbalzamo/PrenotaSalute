package prenotazione.medica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import prenotazione.medica.dto.request.ImpegnativaRequest;
import prenotazione.medica.services.ImpegnativaService;

@RestController
@RequestMapping("/api/impegnative")
public class ImpegnativaController
{
    @Autowired
    private ImpegnativaService impegnativaService;



    @PostMapping("/genera-impegnativa")
    @PreAuthorize("hasAnyRole('MEDICO_CURANTE')")
    public ResponseEntity<?> generaImpegnativa(@RequestBody ImpegnativaRequest request)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(impegnativaService.generaImpegnativa(request));
    }


}