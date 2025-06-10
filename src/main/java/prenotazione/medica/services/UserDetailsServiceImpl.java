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

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AccountRepository repoUtente;


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        Account user = repoUtente.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account non trovato: " + username));

        return UserDetailsImpl.build(user);
    }

}
