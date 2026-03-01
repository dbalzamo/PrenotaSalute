package prenotazione.medica.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Implementazione di {@link org.springframework.security.core.userdetails.UserDetails} che
 * espone i dati di un {@link Account} nel formato richiesto da Spring Security.
 * <p>
 * <b>Ruolo nell'architettura:</b> Spring Security usa UserDetails per rappresentare l'utente
 * autenticato nel {@link org.springframework.security.core.context.SecurityContext}. Questa classe
 * viene creata da {@link prenotazione.medica.services.UserDetailsServiceImpl#loadUserByUsername}
 * a partire da un Account; il metodo statico {@link #build(Account)} è il punto di costruzione.
 * Usata dal filtro JWT, dall'interceptor WebSocket e da qualsiasi componente che debba leggere
 * autorità (ruoli) e identificativo utente dopo il login.
 * </p>
 *
 * @see UserDetails – interfaccia contrattuale di Spring Security per un utente (nome, password, autorità, flag account).
 * @see GrantedAuthority – rappresenta un permesso/ruolo (es. ROLE_PAZIENTE, ROLE_MEDICO_CURANTE).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;


    /**
     * Crea un UserDetailsImpl a partire da un Account. Usato da
     * {@link prenotazione.medica.services.UserDetailsServiceImpl#loadUserByUsername}.
     *
     * @param user account caricato dal DB (non null)
     * @return istanza pronta per essere messa nel SecurityContext
     */
    public static UserDetailsImpl build(Account user) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRuolo().name());
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                List.of(authority)
        );
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /** Sempre true: non gestiamo scadenza account. */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** Sempre true: non blocchiamo account. */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** Sempre true: non gestiamo scadenza credenziali. */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** Sempre true: account abilitato dopo registrazione. */
    @Override
    public boolean isEnabled() {
        return true;
    }

}