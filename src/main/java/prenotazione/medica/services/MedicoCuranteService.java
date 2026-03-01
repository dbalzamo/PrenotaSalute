package prenotazione.medica.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import prenotazione.medica.dto.request.SignupRequest;
import prenotazione.medica.dto.response.SignupResponse;
import prenotazione.medica.dto.PazientePerMessaggioDTO;
import prenotazione.medica.dto.MedicoCuranteListItemDTO;
import prenotazione.medica.model.Account;
import prenotazione.medica.model.MedicoCurante;
import prenotazione.medica.model.Paziente;
import prenotazione.medica.repository.MedicoCuranteRepository;
import prenotazione.medica.repository.PazienteRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione dei medici curanti: ricerca per id/account, creazione in signup,
 * elenco pazienti e elenco medici per signup.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link MedicoCuranteController} per /me, /pazienti,
 * /conversazioni; da {@link AuthController} per signup medico e GET medici-curanti; da
 * {@link MessageService} per costruire le anteprime conversazioni (lista pazienti del medico).
 * </p>
 */
@Service
public class MedicoCuranteService
{
    @Autowired
    private MedicoCuranteRepository medicoCuranteRepository;
    @Autowired
    private PazienteRepository pazienteRepository;
    @Autowired
    ModelMapper modelMapper;


    public MedicoCurante findById(Long medicoCuranteId)
    {
        return Optional.of(medicoCuranteRepository.findById(medicoCuranteId)).get().orElseThrow(() -> new RuntimeException("ERRORE: Medico curante non trovato."));
    }

    public MedicoCurante findByAccountId(Long accountId)
    {
        return Optional.of(medicoCuranteRepository.findByAccountId(accountId)).get().orElseThrow(() -> new RuntimeException("ERRORE: Medico curante non associato a nessun ID account: " + accountId));
    }

    public SignupResponse creazioneMedicoCurante(SignupRequest request, Account account)
    {
        MedicoCurante medicoCurante = modelMapper.map(request, MedicoCurante.class);
        medicoCurante.setAccount(account);
        medicoCuranteRepository.save(medicoCurante);

        return new SignupResponse(true, "Medico curante registrato nel sistema.", null);
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