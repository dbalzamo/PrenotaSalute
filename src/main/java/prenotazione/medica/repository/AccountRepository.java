package prenotazione.medica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prenotazione.medica.model.Account;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>
{
    Optional<Account> findByUsername(String username);

    boolean existsByUsername(String username);

    void deleteByUsername(String username);

    boolean existsByEmail(String email);
}
