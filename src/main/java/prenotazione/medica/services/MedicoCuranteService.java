package prenotazione.medica.services;

import com.prenotasalute.commons.service.AbstractGenericService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import prenotazione.medica.dto.MedicoCuranteDTO;
import prenotazione.medica.dto.request.SignupRequest;
import prenotazione.medica.dto.response.SignupResponse;
import prenotazione.medica.dto.PazientePerMessaggioDTO;
import prenotazione.medica.dto.MedicoCuranteListItemDTO;
import prenotazione.medica.mapper.MedicoCuranteMapper;
import prenotazione.medica.model.Account;
import prenotazione.medica.model.MedicoCurante;
import prenotazione.medica.model.Paziente;
import prenotazione.medica.repository.MedicoCuranteRepository;
import prenotazione.medica.exception.ResourceNotFoundException;
import prenotazione.medica.repository.PazienteRepository;
import prenotazione.medica.services.I18nMessageService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione dei medici curanti: CRUD generico (commons), ricerca per id/account,
 * creazione in signup, elenco pazienti e elenco medici per signup.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link MedicoCuranteController} per /me, /pazienti,
 * /conversazioni; da {@link AuthController} per signup medico e GET medici-curanti; da
 * {@link MessageService} per costruire le anteprime conversazioni (lista pazienti del medico).
 * </p>
 */
@Service
public class MedicoCuranteService extends AbstractGenericService<MedicoCurante, MedicoCuranteDTO, Long> {

    private final MedicoCuranteRepository medicoCuranteRepository;
    private final PazienteRepository pazienteRepository;
    private final ModelMapper modelMapper;
    private final I18nMessageService i18n;

    public MedicoCuranteService(MedicoCuranteRepository medicoCuranteRepository,
                                PazienteRepository pazienteRepository,
                                MedicoCuranteMapper medicoCuranteMapper,
                                ModelMapper modelMapper,
                                I18nMessageService i18n) {
        super(medicoCuranteRepository, medicoCuranteMapper);
        this.medicoCuranteRepository = medicoCuranteRepository;
        this.pazienteRepository = pazienteRepository;
        this.modelMapper = modelMapper;
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
     */
    public MedicoCuranteDTO findByAccountIdAsDto(Long accountId) {
        return mapper.toDTO(findByAccountId(accountId));
    }

    public SignupResponse creazioneMedicoCurante(SignupRequest request, Account account)
    {
        MedicoCurante medicoCurante = modelMapper.map(request, MedicoCurante.class);
        medicoCurante.setAccount(account);
        medicoCuranteRepository.save(medicoCurante);

        return new SignupResponse(true, i18n.getMessage("medico.registered"), null);
    }

    /** Elenco pazienti associati al medico (per messaggistica). */
    public List<PazientePerMessaggioDTO> findPazientiForMessaging(Long medicoCuranteAccountId) {
        MedicoCurante medico = findByAccountId(medicoCuranteAccountId);
        return pazienteRepository.findByMedicoCurante_Id(medico.getId()).stream()
                .map(p -> new PazientePerMessaggioDTO(
                        p.getId(),
                        p.getNome(),
                        p.getCognome(),
                        p.getAccount() != null ? p.getAccount().getId() : null
                ))
                .collect(Collectors.toList());
    }

    /** Elenco medici curanti per selezione in signup (id, nome, cognome). Endpoint pubblico. */
    public List<MedicoCuranteListItemDTO> findAllForSignup() {
        return medicoCuranteRepository.findAll().stream()
                .map(m -> new MedicoCuranteListItemDTO(m.getId(), m.getNome(), m.getCognome()))
                .collect(Collectors.toList());
    }
}