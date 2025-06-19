package prenotazione.medica.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prenotazione.medica.dto.request.SignupRequest;
import prenotazione.medica.dto.response.SignupResponse;
import prenotazione.medica.dto.request.AuthRequest;
import prenotazione.medica.dto.response.AuthResponse;
import prenotazione.medica.services.AccountService;
import prenotazione.medica.services.MedicoCuranteService;
import prenotazione.medica.services.PazienteService;

@RestController
@RequestMapping("/api/auth")
public class AuthController
{
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AccountService accountService;
    @Autowired
    private PazienteService pazienteService;
    @Autowired
    private MedicoCuranteService medicoCuranteService;


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest request) {
        logger.info("START AuthController::authenticateUser with username: {}", request.getUsername());
        AuthResponse authResponse = accountService.loginAccount(request);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, authResponse.getCookie().toString())
                .body(authResponse);
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser()
    {
        logger.info("START AuthController::signout");
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accountService.logoutAccount().toString()).body("Account disconnesso.");
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request)
    {
        // Creazione account utente
        SignupResponse signupResponse = accountService.creazioneAccount(request);

        if (!signupResponse.getIsSuccess())
        {
            return ResponseEntity.badRequest().body(signupResponse.getMessage());
        }

        // Creazione utente
        switch (request.getRuolo())
        {
            case PAZIENTE:
                signupResponse = pazienteService.creazionePaziente(request, signupResponse.getAccount());
                return ResponseEntity.ok(signupResponse.getMessage());
            case MEDICO_CURANTE:
                signupResponse = medicoCuranteService.creazioneMedicoCurante(request, signupResponse.getAccount());
                return ResponseEntity.ok(signupResponse.getMessage());
        }

        accountService.deleteAccount(signupResponse.getAccount().getUsername());
        return ResponseEntity.badRequest().body("Registrazione annullata, riprova più tardi!");
    }



}