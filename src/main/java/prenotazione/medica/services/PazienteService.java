package prenotazione.medica.services;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prenotazione.medica.dto.PazienteDTO;
import prenotazione.medica.dto.request.SignupRequest;
import prenotazione.medica.dto.response.SignupResponse;
import prenotazione.medica.dto.response.MedicoCuranteResponse;
import prenotazione.medica.model.Account;
import prenotazione.medica.model.MedicoCurante;
import prenotazione.medica.model.Paziente;
import prenotazione.medica.repository.MedicoCuranteRepository;
import prenotazione.medica.repository.PazienteRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

/**
 * Servizio per la gestione dei pazienti: ricerca per account, creazione in signup, profilo,
 * medico curante associato (GET/PUT) e conversione in DTO per API.
 * <p>
 * <b>Ruolo nell'architettura:</b> usato da {@link PazienteController} per /me, /mio-medico,
 * /mio-medico (PUT), da {@link AuthController} per signup paziente (con eventuale medico curante).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PazienteService
{
    private final PazienteRepository pazienteRepository;
    private final MedicoCuranteRepository medicoCuranteRepository;
    private final ModelMapper modelMapper;

    public Paziente findByAccountId(Long accountId)
    {
        return pazienteRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("ERRORE: Paziente non associato a nessun ID account: " + accountId));
    }


    public SignupResponse creazionePaziente(SignupRequest request, Account account)
    {
        Paziente paziente = modelMapper.map(request, Paziente.class);
        paziente.setAccount(account);
        if (request.getMedicoCuranteId() != null) {
            MedicoCurante medico = medicoCuranteRepository.findById(request.getMedicoCuranteId())
                    .orElse(null);
            if (medico != null) {
                paziente.setMedicoCurante(medico);
            }
        }
        pazienteRepository.save(paziente);

        return new SignupResponse(true, "Paziente registrato nel sistema.", null);
    }

    /**
     * Aggiorna il paziente associato all'account (id = accountId).
     * Modifica il record esistente senza crearne di nuovi.
     */
    public String updatePaziente(Long accountId, PazienteDTO pazienteDTO) {
        Paziente existing = findByAccountId(accountId);
        existing.setNome(pazienteDTO.getNome());
        existing.setCognome(pazienteDTO.getCognome());
        existing.setCodiceFiscale(pazienteDTO.getCodiceFiscale());
        existing.setIndirizzoDiResidenza(pazienteDTO.getIndirizzoDiResidenza());
        if (pazienteDTO.getDataDiNascita() != null && !pazienteDTO.getDataDiNascita().isEmpty()) {
            existing.setDataDiNascita(parseDataDiNascita(pazienteDTO.getDataDiNascita()));
        }
        if (existing.getAccount() != null && pazienteDTO.getEmail() != null) {
            existing.getAccount().setEmail(pazienteDTO.getEmail());
        }
        pazienteRepository.save(existing);
        return "Paziente modificato con successo nel sistema.";
    }

    private static Date parseDataDiNascita(String dataDiNascita) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dataDiNascita);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Data di nascita non valida: " + dataDiNascita);
        }
    }

    /**
     * Restituisce il medico curante come DTO per la Posta (evita LazyInitializationException).
     * Ritorna null se il paziente non ha un medico assegnato o l'account non è un paziente.
     */
    @Transactional(readOnly = true)
    public MedicoCuranteResponse getMedicoCuranteResponseByPazienteAccountId(Long accountId) {
        Optional<Paziente> optPaziente = pazienteRepository.findByAccountId(accountId);
        if (optPaziente.isEmpty()) return null;
        Paziente p = optPaziente.get();
        MedicoCurante medico = p.getMedicoCurante();
        if (medico == null) return null;
        MedicoCuranteResponse.AccountInfo accountInfo = null;
        if (medico.getAccount() != null) {
            accountInfo = new MedicoCuranteResponse.AccountInfo(
                    medico.getAccount().getId(),
                    medico.getAccount().getUsername(),
                    medico.getAccount().getEmail()
            );
        }
        return new MedicoCuranteResponse(medico.getId(), medico.getNome(), medico.getCognome(), accountInfo);
    }

    /**
     * Associa il medico curante al paziente loggato. Solo ruolo PAZIENTE.
     */
    public void setMedicoCurante(Long accountId, Long medicoCuranteId) {
        Paziente p = findByAccountId(accountId);
        MedicoCurante medico = medicoCuranteRepository.findById(medicoCuranteId)
                .orElseThrow(() -> new RuntimeException("Medico curante non trovato: " + medicoCuranteId));
        p.setMedicoCurante(medico);
        pazienteRepository.save(p);
    }
}