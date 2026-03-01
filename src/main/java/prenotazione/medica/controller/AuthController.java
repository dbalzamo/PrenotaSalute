package prenotazione.medica.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prenotazione.medica.dto.request.SignupRequest;
import prenotazione.medica.dto.response.SignupResponse;
import prenotazione.medica.dto.MedicoCuranteListItemDTO;
import prenotazione.medica.dto.request.AuthRequest;
import prenotazione.medica.dto.response.AuthResponse;
import prenotazione.medica.services.AccountService;
import prenotazione.medica.services.MedicoCuranteService;
import prenotazione.medica.services.PazienteService;

import java.util.List;

/**
 * Controller per autenticazione e registrazione (login, logout, signup, elenco medici).
 * <p>
 * <b>Ruolo nell'architettura:</b> espone POST /api/auth/login, /logout, /signup e GET /api/auth/medici-curanti.
 * Il login delega a {@link AccountService#loginAccount} e restituisce {@link AuthResponse} con JWT e cookie;
 * il signup crea prima l'account poi il profilo (Paziente o MedicoCurante) tramite i rispettivi service.
 * Le richieste a /api/auth/** sono pubbliche (configurate in {@link prenotazione.medica.security.config.WebSecurityConfig}).
 * </p>
 */
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

    /** Elenco medici curanti per selezione in registrazione (paziente). Endpoint pubblico. */
    @GetMapping("/medici-curanti")
    public ResponseEntity<List<MedicoCuranteListItemDTO>> getMediciCuranti() {
        return ResponseEntity.ok(medicoCuranteService.findAllForSignup());
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request)
    {
        SignupResponse signupResponse = accountService.creazioneAccount(request);

        if (!signupResponse.getIsSuccess())
        {
            return ResponseEntity.badRequest().body(signupResponse.getMessage());
        }

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