package prenotazione.medica.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prenotazione.medica.auth.dto.request.SignupRequest;
import prenotazione.medica.auth.dto.response.SignupResponse;
import prenotazione.medica.medico.service.MedicoCuranteService;
import prenotazione.medica.paziente.service.PazienteService;
import prenotazione.medica.shared.i18n.I18nMessageService;

/**
 * Orchestrazione della registrazione: account e profilo (paziente/medico) nella stessa transazione,
 * così un fallimento sulla creazione del profilo non lascia account orfani nel database.
 */
@Service
@RequiredArgsConstructor
public class AuthRegistrationService {

    private final AccountService accountService;
    private final PazienteService pazienteService;
    private final MedicoCuranteService medicoCuranteService;
    private final I18nMessageService i18n;

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> signup(SignupRequest request) {
        SignupResponse signupResponse = accountService.creazioneAccount(request);

        if (!signupResponse.getIsSuccess()) {
            return ResponseEntity.badRequest().body(signupResponse.getMessage());
        }

        switch (request.getRuolo()) {
            case PAZIENTE:
                signupResponse = pazienteService.creazionePaziente(request, signupResponse.getAccount());
                return ResponseEntity.ok(signupResponse.getMessage());
            case MEDICO_CURANTE:
                signupResponse = medicoCuranteService.creazioneMedicoCurante(request, signupResponse.getAccount());
                return ResponseEntity.ok(signupResponse.getMessage());
            default:
                accountService.deleteAccount(signupResponse.getAccount().getUsername());
                return ResponseEntity.badRequest().body(i18n.getMessage("auth.signup.cancelled"));
        }
    }
}
