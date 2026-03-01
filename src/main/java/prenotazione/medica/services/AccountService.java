package prenotazione.medica.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import prenotazione.medica.controller.AuthController;
import prenotazione.medica.dto.request.AuthRequest;
import prenotazione.medica.dto.response.AuthResponse;
import prenotazione.medica.model.Account;
import prenotazione.medica.dto.request.SignupRequest;
import prenotazione.medica.dto.response.SignupResponse;
import prenotazione.medica.model.UserDetailsImpl;
import prenotazione.medica.repository.AccountRepository;
import prenotazione.medica.security.jwt.JwtService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione degli account: login, logout, registrazione e validazione.
 * <p>
 * <b>Ruolo nell'architettura:</b> invocato da {@link prenotazione.medica.controller.AuthController}
 * per login (genera JWT e cookie), logout (cookie vuoto) e signup (creazione account + eventuale
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

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtService jwtService;


    public AuthResponse loginAccount(AuthRequest request)
    {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie cookie = jwtService.generateCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new AuthResponse(userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles, cookie.getValue(), cookie);
    }


    public ResponseCookie logoutAccount()
    {
        return jwtService.getCleanJwtCookie();
    }


    public SignupResponse creazioneAccount(SignupRequest request)
    {
        logger.info("START AccountService:: creazione account utente...");

        if (accountRepository.existsByUsername(request.getUsername())) {
            return new SignupResponse(false,"Username già presente nel sistema!", null);
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            return new SignupResponse(false,"Email già presente nel sistema!", null);
        }

        Account account = new Account(request.getUsername(), request.getEmail(), encoder.encode(request.getPassword()), request.getRuolo());
        accountRepository.save(account);

        return new SignupResponse(true, "Registrazione avvenuta con successo!", account);
    }


    public void deleteAccount(String username)
    {
        accountRepository.deleteByUsername(username);
    }

    public Optional<Account> findById(Long accountId)
    {
        return accountRepository.findById(accountId);
    }


}