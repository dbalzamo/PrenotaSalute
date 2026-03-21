package prenotazione.medica.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import prenotazione.medica.auth.api.AuthController;
import prenotazione.medica.auth.dto.request.AuthRequest;
import prenotazione.medica.auth.dto.request.SignupRequest;
import prenotazione.medica.auth.dto.response.AuthResponse;
import prenotazione.medica.auth.dto.response.SignupResponse;
import prenotazione.medica.auth.entity.Account;
import prenotazione.medica.auth.entity.UserDetailsImpl;
import prenotazione.medica.auth.repository.AccountRepository;
import prenotazione.medica.auth.jwt.JwtService;
import prenotazione.medica.shared.i18n.I18nMessageService;
import prenotazione.medica.auth.SecurityUtils;
import prenotazione.medica.shared.exception.BadRequestException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione degli account: login, logout, registrazione e validazione.
 * <p>
 * <b>Ruolo nell'architettura:</b> invocato da {@link AuthController}
 * per login (genera JWT in body), logout e signup (creazione account + eventuale
 * associazione medico in signup paziente). Usa {@link AuthenticationManager} per validare
 * username/password, {@link JwtService} per generare il token e il cookie, {@link AccountRepository}
 * per persistenza. La creazione del profilo (Paziente o MedicoCurante) dopo la creazione account
 * è delegata a PazienteService e MedicoCuranteService dal controller.
 * </p>
 */
@Service
public class AccountService
{
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AccountRepository accountRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final I18nMessageService i18n;

    public AccountService(AccountRepository accountRepository,
                          AuthenticationManager authenticationManager,
                          PasswordEncoder encoder,
                          JwtService jwtService,
                          I18nMessageService i18n) {
        this.accountRepository = accountRepository;
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.i18n = i18n;
    }

    public AuthResponse loginAccount(AuthRequest request)
    {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtService.generateTokenFromUsername(userDetails.getUsername());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new AuthResponse(userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles, token);
    }

    /**
     * Logout: il client rimuove il token in memoria. Nessun cookie da invalidare.
     */
    public void logoutAccount() {
        // Stateless JWT: nessuna azione lato server; il client non invierà più il Bearer token
    }


    public SignupResponse creazioneAccount(SignupRequest request)
    {
        logger.info("START AccountService:: creazione account utente...");

        if (accountRepository.existsByUsername(request.getUsername())) {
            return new SignupResponse(false, i18n.getMessage("auth.signup.error.username.exists"), null);
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            return new SignupResponse(false, i18n.getMessage("auth.signup.error.email.exists"), null);
        }

        Account account = new Account(request.getUsername(), request.getEmail(), encoder.encode(request.getPassword()), request.getRuolo());
        accountRepository.save(account);

        return new SignupResponse(true, i18n.getMessage("auth.signup.success"), account);
    }


    public void deleteAccount(String username)
    {
        accountRepository.deleteByUsername(username);
    }

    public Optional<Account> findById(Long accountId)
    {
        return accountRepository.findById(accountId);
    }

    /**
     * Cambia la password dell'account corrente previa verifica della oldPassword.
     */
    public void changePassword(String oldPassword, String newPassword) {
        Long accountId = SecurityUtils.getCurrentAccountId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BadRequestException("account.notfound", accountId));

        if (!encoder.matches(oldPassword, account.getPassword())) {
            throw new BadRequestException("auth.password.old.mismatch");
        }

        account.setPassword(encoder.encode(newPassword));
        accountRepository.save(account);
    }

    /**
     * Cambia lo username dell'account corrente, garantendo l'unicità.
     */
    public void changeUsername(String newUsername) {
        Long accountId = SecurityUtils.getCurrentAccountId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BadRequestException("account.notfound", accountId));

        if (accountRepository.existsByUsername(newUsername)) {
            throw new BadRequestException("auth.signup.error.username.exists");
        }

        account.setUsername(newUsername);
        accountRepository.save(account);
    }
}