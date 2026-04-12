package prenotazione.medica.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import prenotazione.medica.auth.SecurityUtils;
import prenotazione.medica.auth.dto.response.AuthResponse;
import prenotazione.medica.auth.entity.Account;
import prenotazione.medica.auth.entity.UserDetailsImpl;
import prenotazione.medica.auth.jwt.JwtService;
import prenotazione.medica.auth.repository.AccountRepository;
import prenotazione.medica.shared.enums.ERuolo;
import prenotazione.medica.shared.i18n.I18nMessageService;

/**
 * Test unitari per {@link AccountService} (cambio username e nuovo JWT).
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private I18nMessageService i18n;

    @InjectMocks
    private AccountService accountService;

    @Test
    void changeUsername_persistsNewUsernameAndReturnsAuthResponseWithFreshToken() {
        Account account = new Account("vecchio", "u@mail.it", "hash", ERuolo.PAZIENTE);
        account.setId(10L);

        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentAccountId).thenReturn(10L);
            when(accountRepository.findById(10L)).thenReturn(Optional.of(account));
            when(accountRepository.existsByUsername("nuovo")).thenReturn(false);
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateToken(any(UserDetailsImpl.class))).thenReturn("new-jwt");
            when(i18n.getMessage("auth.username.changed")).thenReturn("Username aggiornato.");

            AuthResponse response = accountService.changeUsername("nuovo");

            assertThat(response.getUsername()).isEqualTo("nuovo");
            assertThat(response.getToken()).isEqualTo("new-jwt");
            assertThat(response.getMessage()).isEqualTo("Username aggiornato.");
            assertThat(response.getId()).isEqualTo(10L);
            verify(jwtService).generateToken(any(UserDetailsImpl.class));
        }
    }
}
