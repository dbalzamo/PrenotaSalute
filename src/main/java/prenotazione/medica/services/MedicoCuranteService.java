package prenotazione.medica.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import prenotazione.medica.dto.request.SignupRequest;
import prenotazione.medica.dto.response.SignupResponse;
import prenotazione.medica.model.Account;
import prenotazione.medica.model.MedicoCurante;
import prenotazione.medica.repository.MedicoCuranteRepository;

import java.util.Optional;

@Service
public class MedicoCuranteService
{
    @Autowired
    private MedicoCuranteRepository medicoCuranteRepository;
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

}