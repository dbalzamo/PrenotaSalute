package prenotazione.medica.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import prenotazione.medica.auth.entity.UserDetailsImpl;
import prenotazione.medica.chat.api.ChatController;
import prenotazione.medica.shared.security.StompSecurityContextInterceptor;
import prenotazione.medica.shared.exception.UnauthorizedException;

/**
 * Utility per ottenere l'id dell'account corrente dal contesto di sicurezza Spring.
 * <p>
 * <b>Ruolo nell'architettura:</b> usata dai controller REST che devono identificare l'utente
 * autenticato (es. per verificare che userId coincida con l'account loggato, o per impostare
 * senderId/owner). Funziona solo quando il {@link SecurityContext}
 * è popolato (richieste HTTP dopo il filtro JWT, o messaggi STOMP dopo
 * {@link StompSecurityContextInterceptor}). Nel WebSocket il
 * {@link ChatController} preferisce usare il {@link java.security.Principal}
 * iniettato dal messaggio per evitare dipendere dal SecurityContext nel thread del broker.
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