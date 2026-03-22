package prenotazione.medica.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import prenotazione.medica.auth.dto.request.AuthRequest;
import prenotazione.medica.auth.dto.request.SignupRequest;
import prenotazione.medica.auth.dto.request.ChangePasswordRequest;
import prenotazione.medica.auth.dto.request.ChangeUsernameRequest;
import prenotazione.medica.auth.dto.response.AuthResponse;
import prenotazione.medica.auth.service.AccountService;
import prenotazione.medica.auth.service.AuthRegistrationService;
import prenotazione.medica.medico.dto.MedicoCuranteListItemDTO;
import prenotazione.medica.shared.i18n.I18nMessageService;
import prenotazione.medica.medico.service.MedicoCuranteService;

import java.util.List;

/**
 * Controller per autenticazione e registrazione (login, logout, signup, elenco medici).
 * <p>
 * <b>Ruolo nell'architettura:</b> espone POST /api/auth/login, /logout, /signup e GET /api/auth/medici-curanti.
 * Il login delega a {@link AccountService#loginAccount} e restituisce {@link AuthResponse} con JWT nel body (header Bearer per le chiamate successive);
 * il signup è orchestrato da {@link AuthRegistrationService} (stessa transazione: account + profilo).
 * Le richieste a /api/auth/** sono pubbliche (configurate in {@link prenotazione.medica.shared.security.config.WebSecurityConfig}).
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticazione", description = "Endpoint pubblici per autenticazione, logout e registrazione utenti.")
public class AuthController
{
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AccountService accountService;
    @Autowired
    private MedicoCuranteService medicoCuranteService;
    @Autowired
    private I18nMessageService i18n;
    @Autowired
    private AuthRegistrationService authRegistrationService;

    @PostMapping("/login")
    @Operation(
            summary = "Autentica un utente",
            description = "Esegue il login con username e password e restituisce il JWT da usare come Bearer token nelle chiamate successive."
    )
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid AuthRequest request) {
        logger.info("START AuthController::authenticateUser with username: {}", request.getUsername());
        AuthResponse authResponse = accountService.loginAccount(request);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout utente corrente",
            description = "Effettua il logout logico dell'utente corrente (invalidazione lato applicativo del contesto)."
    )
    public ResponseEntity<String> logoutUser() {
        logger.info("START AuthController::signout");
        accountService.logoutAccount();
        return ResponseEntity.ok(i18n.getMessage("auth.logout"));
    }

    /** Elenco medici curanti per selezione in registrazione (paziente). Endpoint pubblico. */
    @GetMapping("/medici-curanti")
    @Operation(
            summary = "Elenco medici curanti",
            description = "Restituisce la lista dei medici curanti disponibili, usata dal paziente in fase di registrazione."
    )
    public ResponseEntity<List<MedicoCuranteListItemDTO>> getMediciCuranti() {
        return ResponseEntity.ok(medicoCuranteService.findAllForSignup());
    }

    @PostMapping("/signup")
    @Operation(
            summary = "Registrazione nuovo utente",
            description = "Registra un nuovo account e crea il relativo profilo (paziente o medico curante) in base al ruolo indicato."
    )
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest request) {
        return authRegistrationService.signup(request);
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Cambio password account corrente",
            description = "Permette all'utente autenticato di cambiare la propria password fornendo quella attuale e la nuova."
    )
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        accountService.changePassword(request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(i18n.getMessage("auth.password.changed"));
    }

    @PutMapping("/change-username")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Cambio username account corrente",
            description = "Aggiorna lo username e restituisce un nuovo JWT (stesso formato del login): "
                    + "il client deve sostituire il Bearer token perché il precedente contiene il vecchio subject."
    )
    public ResponseEntity<AuthResponse> changeUsername(@RequestBody @Valid ChangeUsernameRequest request) {
        return ResponseEntity.ok(accountService.changeUsername(request.getNewUsername()));
    }

}