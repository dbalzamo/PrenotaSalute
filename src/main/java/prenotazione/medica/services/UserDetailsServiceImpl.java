package prenotazione.medica.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prenotazione.medica.repository.AccountRepository;
import prenotazione.medica.model.Account;
import prenotazione.medica.model.UserDetailsImpl;

/**
 * Implementazione di {@link UserDetailsService}: carica un utente per username (login) e lo
 * restituisce come {@link UserDetailsImpl} per Spring Security.
 * <p>
 * <b>Ruolo nell'architettura:</b> usata dal {@link DaoAuthenticationProvider} per il login
 * form (se abilitato) e soprattutto da {@link prenotazione.medica.security.jwt.JwtAuthenticationFilter}
 * e da {@link prenotazione.medica.config.StompSecurityContextInterceptor} per caricare i dettagli
 * utente dopo la validazione del JWT (username → Account → UserDetailsImpl con id e ruoli).
 * </p>
 *
 * @see UserDetailsService – interfaccia contrattuale Spring Security per il caricamento utente per username.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AccountRepository repoUtente;
    @Autowired
    private I18nMessageService i18n;

    /**
     * Carica l'account per username e lo converte in UserDetailsImpl (id, username, password, autorità).
     * Invocato dal filtro JWT e dall'interceptor WebSocket dopo aver estratto lo username dal token.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account user = repoUtente.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(i18n.getMessage("account.notfound", username)));

        return UserDetailsImpl.build(user);
    }

}
