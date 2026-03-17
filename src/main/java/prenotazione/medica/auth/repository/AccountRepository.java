package prenotazione.medica.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.auth.service.UserDetailsServiceImpl;
import prenotazione.medica.auth.entity.Account;

import java.util.Optional;

/**
 * Accesso ai dati degli {@link Account} (credenziali e ruolo).
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link UserDetailsServiceImpl}
 * per il login (caricamento utente per Spring Security) e da AccountService
 * per registrazione e verifica esistenza username/email.
 * </p>
 *
 * @see JpaRepository – interfaccia Spring Data che fornisce findById, save, delete, ecc. senza implementazione.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long>
{
    /** Caricamento per autenticazione (username = login). */
    Optional<Account> findByUsername(String username);

    boolean existsByUsername(String username);

    void deleteByUsername(String username);

    boolean existsByEmail(String email);
}
