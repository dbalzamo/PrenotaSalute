package prenotazione.medica.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import prenotazione.medica.auth.entity.UserDetailsImpl;
import prenotazione.medica.shared.exception.UnauthorizedException;

/**
 * Utility per ottenere l'id dell'account corrente dal contesto di sicurezza Spring.
 * <p>
 * <b>Ruolo nell'architettura:</b> usata dai controller REST che devono identificare l'utente
 * autenticato (es. per verificare che userId coincida con l'account loggato, o per impostare
 * senderId/owner). Funziona solo quando il contesto di sicurezza è popolato
 * (richieste HTTP dopo il filtro JWT).
 * </p>
 *
 * @see SecurityContextHolder – contenitore thread-local che tiene l'autenticazione corrente.
 * @see UserDetailsImpl – Principal usato dopo login (contiene id account e ruoli).
 */
public class SecurityUtils
{
    /**
     * Restituisce l'id dell'account dell'utente attualmente autenticato.
     * @return id dell'account (da UserDetailsImpl)
     * @throws RuntimeException se nessun utente è autenticato (SecurityContext vuoto o principal non UserDetailsImpl)
     */
    public static Long getCurrentAccountId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("account.notauthenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return  userDetails.getId();
    }
}