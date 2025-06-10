package prenotazione.medica.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import prenotazione.medica.dto.request.SignupRequest;
import prenotazione.medica.dto.response.SignupResponse;
import prenotazione.medica.model.Account;
import prenotazione.medica.model.Paziente;
import prenotazione.medica.repository.PazienteRepository;

import java.util.Optional;

@Service
public class PazienteService
{
    @Autowired
    private PazienteRepository pazienteRepository;
    @Autowired
    ModelMapper modelMapper;

    public Paziente findByAccountId(Long accountId)
    {
        return Optional.ofNullable(pazienteRepository.findByAccountId(accountId)).get().orElseThrow(() -> new RuntimeException("ERRORE: Paziente non associato a nessun ID account: " + accountId));
    }


    public SignupResponse creazionePaziente(SignupRequest request, Account account)
    {
        Paziente paziente = modelMapper.map(request, Paziente.class);
        paziente.setAccount(account);
        pazienteRepository.save(paziente);

        return new SignupResponse(true, "Paziente registrato nel sistema.", null);
    }
}