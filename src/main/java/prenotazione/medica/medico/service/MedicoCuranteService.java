package prenotazione.medica.medico.service;

import com.prenotasalute.commons.mapper.GenericMapper;
import com.prenotasalute.commons.service.AbstractGenericService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prenotazione.medica.auth.dto.request.SignupRequest;
import prenotazione.medica.auth.entity.Account;
import prenotazione.medica.auth.repository.AccountRepository;
import prenotazione.medica.auth.dto.response.SignupResponse;
import prenotazione.medica.medico.mapper.MedicoCuranteMapper;
import prenotazione.medica.medico.repository.MedicoCuranteRepository;
import prenotazione.medica.medico.api.MedicoCuranteController;
import prenotazione.medica.medico.dto.MedicoCuranteDTO;
import prenotazione.medica.medico.dto.MedicoCuranteListItemDTO;
import prenotazione.medica.medico.entity.MedicoCurante;
import prenotazione.medica.shared.exception.ResourceNotFoundException;
import prenotazione.medica.shared.i18n.I18nMessageService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione dei medici curanti: CRUD generico (commons), ricerca per id/account,
 * creazione in signup, elenco pazienti e elenco medici per signup.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link MedicoCuranteController} per /me, /pazienti,
 * /conversazioni; da prenotazione.medica.auth.api.AuthController per signup medico e GET medici-curanti;
 * da MessageService per costruire le anteprime conversazioni (lista pazienti del medico).
 * </p>
 */
@Service
public class MedicoCuranteService extends AbstractGenericService<MedicoCurante, MedicoCuranteDTO, Long> {

    private final MedicoCuranteRepository medicoCuranteRepository;
    private final AccountRepository accountRepository;
    /** Riferimento tipizzato per metodi MapStruct oltre {@link com.prenotasalute.commons.mapper.GenericMapper} (es. signup). */
    private final MedicoCuranteMapper medicoCuranteMapper;
    private final I18nMessageService i18n;

    public MedicoCuranteService(MedicoCuranteRepository medicoCuranteRepository,
                                AccountRepository accountRepository,
                                MedicoCuranteMapper medicoCuranteMapper,
                                I18nMessageService i18n) {
        super(medicoCuranteRepository, medicoCuranteMapper);
        this.medicoCuranteRepository = medicoCuranteRepository;
        this.accountRepository = accountRepository;
        this.medicoCuranteMapper = medicoCuranteMapper;
        this.i18n = i18n;
    }

    public MedicoCurante findById(Long medicoCuranteId)
    {
        return medicoCuranteRepository.findById(medicoCuranteId)
                .orElseThrow(() -> new ResourceNotFoundException("medico.notfound"));
    }

    public MedicoCurante findByAccountId(Long accountId) {
        return medicoCuranteRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("account.notfound.for", accountId));
    }

    /**
     * Restituisce il medico curante associato all'account come DTO (per endpoint /me).
     * Transazione attiva necessaria: {@code account} è LAZY e il mapper legge {@code account.email}.
     */
    @Transactional(readOnly = true)
    public MedicoCuranteDTO findByAccountIdAsDto(Long accountId) {
        return mapper.toDTO(findByAccountId(accountId));
    }

    public SignupResponse creazioneMedicoCurante(SignupRequest request, Account account)
    {
        MedicoCurante medicoCurante = medicoCuranteMapper.toEntityFromSignupRequest(request);
        medicoCurante.setAccount(accountRepository.getReferenceById(account.getId()));
        medicoCuranteRepository.save(medicoCurante);

        return new SignupResponse(true, i18n.getMessage("medico.registered"), null);
    }

    /** Elenco medici curanti per selezione in signup (id, nome, cognome). Endpoint pubblico. */
    public List<MedicoCuranteListItemDTO> findAllForSignup() {
        return medicoCuranteRepository.findAll().stream()
                .map(m -> new MedicoCuranteListItemDTO(m.getId(), m.getNome(), m.getCognome()))
                .collect(Collectors.toList());
    }
}

