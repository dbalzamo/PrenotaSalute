package prenotazione.medica.security.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import prenotazione.medica.model.UserDetailsImpl;

public class SecurityUtils
{
    public static Long getCurrentAccountId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Account non autenticato");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return  userDetails.getId();
    }
}