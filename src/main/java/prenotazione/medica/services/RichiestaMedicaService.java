package prenotazione.medica.services;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import prenotazione.medica.controller.AuthController;
import prenotazione.medica.dto.request.RichiestaMedicaRequest;
import prenotazione.medica.dto.request.RifiutoRichiestaRequest;
import prenotazione.medica.enums.ERuolo;
import prenotazione.medica.enums.EStatoRichiesta;
import prenotazione.medica.enums.ETipoRichiesta;
import prenotazione.medica.model.Account;
import prenotazione.medica.model.MedicoCurante;
import prenotazione.medica.model.Paziente;
import prenotazione.medica.model.RichiestaMedica;
import prenotazione.medica.repository.RichiestaMedicaRepository;
import prenotazione.medica.security.utils.SecurityUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class RichiestaMedicaService
{
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private RichiestaMedicaRepository richiestaMedicaRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AccountService accountService;
    @Autowired
    private PazienteService pazienteService;
    @Autowired
    private MedicoCuranteService medicoCuranteService;


    public RichiestaMedica findById(Long idRichiestaMedica)
    {
        return Optional.of(richiestaMedicaRepository.findById(idRichiestaMedica)).get().orElseThrow(() -> new RuntimeException("ERRORE: Richiesta medica non trovata con ID: " + idRichiestaMedica));
    }

    public String creaRichiestaMedica(RichiestaMedicaRequest request)
    {
        RichiestaMedica richiestaMedica = modelMapper.map(request, RichiestaMedica.class);

        richiestaMedica.setPaziente(pazienteService.findByAccountId(SecurityUtils.getCurrentAccountId()));
        richiestaMedica.setMedicoCurante(medicoCuranteService.findById(request.getIdMedico()));
        richiestaMedica.setTipoRichiesta(ETipoRichiesta.valueOf(request.getTipoRichiesta()));
        richiestaMedica.setDataEmissione(new Date());
        richiestaMedica.setStato(EStatoRichiesta.INVIATA);

        richiestaMedicaRepository.save(richiestaMedica);
        return "Richiesta creata con successo";
    }


    public List<RichiestaMedica> findAllByStatoAndPazienteId(EStatoRichiesta stato)
    {
        // Recupero l'account dell'utente (Paziente/medico) autenticato
        Optional<Account> accountOptional = accountService.findById(SecurityUtils.getCurrentAccountId());

        if (accountOptional.isPresent())
        {
            Account accountUtente = accountOptional.get(); // Estrazione dell'account dall'optional

            if (accountUtente.getRuolo() == ERuolo.PAZIENTE)
            {
                Paziente paziente = pazienteService.findByAccountId(accountUtente.getId());
                return richiestaMedicaRepository.findAllByStatoAndPaziente_Id(stato, paziente.getId());
            }

            if (accountUtente.getRuolo() == ERuolo.MEDICO_CURANTE)
            {
                MedicoCurante medicoCurante = medicoCuranteService.findByAccountId(accountUtente.getId());
                return richiestaMedicaRepository.findAllByStatoAndMedicoCurante_Id(stato, medicoCurante.getId());
            }
        }

        return Collections.emptyList();
    }

    public String visualizzaRichiestaMedica(Long id)
    {
        Optional<RichiestaMedica> richiestaMedica = richiestaMedicaRepository.findById(id);

        if (richiestaMedica.isPresent()){
            richiestaMedica.get().setStato(EStatoRichiesta.VISUALIZZATA);
            richiestaMedicaRepository.save(richiestaMedica.get());
            return "Richiesta ID: " + id + " visualizzata!";
        }
        throw new RuntimeException("ERRORE: richiesta selezionata non è presente nel sistema con ID: " + id);
    }

    public String rifiutaRichiestaMedica(RifiutoRichiestaRequest rifiutoRichiestaRequest)
    {
        Optional<RichiestaMedica> richiestaMedica = richiestaMedicaRepository.findById(rifiutoRichiestaRequest.getIdRichiesta());

        if (richiestaMedica.isPresent()){
            richiestaMedica.get().setStato(EStatoRichiesta.RIFIUTATA);
            richiestaMedica.get().setDescrizione(rifiutoRichiestaRequest.getMotivazione());
            richiestaMedicaRepository.save(richiestaMedica.get());
            return "Richiesta ID: " + rifiutoRichiestaRequest.getIdRichiesta() + " rifiutata: '" + rifiutoRichiestaRequest.getMotivazione() + "'";
        }
        throw new RuntimeException("ERRORE: richiesta selezionata non è presente nel sistema con ID: " + rifiutoRichiestaRequest.getIdRichiesta());
    }

    public Optional<RichiestaMedica> accettaRichiestaMedica(Long idRichiestaMedica)
    {
        Optional<RichiestaMedica> richiestaMedica = richiestaMedicaRepository.findById(idRichiestaMedica);

        if (richiestaMedica.isPresent()){
            richiestaMedica.get().setStato(EStatoRichiesta.ACCETTATA);
            richiestaMedica.get().setDataAccettazione(new Date());
            richiestaMedicaRepository.save(richiestaMedica.get());
            return richiestaMedica;
        }
        throw new RuntimeException("ERRORE: richiesta selezionata non è presente nel sistema con ID: " + idRichiestaMedica);
    }

    /***
     * fixedRate fa partire il metodo ogni intervallo di tempo a prescindere dal tempo di esecuzione
     * (può partire anche se la precedente esecuzione non è ancora finita).
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void aggiornaStatiScaduti() {
        List<RichiestaMedica> richiesteAccettate = richiestaMedicaRepository.findAllByStato(EStatoRichiesta.ACCETTATA);

        for (RichiestaMedica richiesta : richiesteAccettate) {
            if (richiesta.getDataAccettazione() != null) {
                long minutiTrascorsi = Duration.between(
                        richiesta.getDataAccettazione().toInstant(),
                        Instant.now()
                ).toMinutes();

                if (minutiTrascorsi >= 1) { // per test, 1 minuto
                    richiesta.setStato(EStatoRichiesta.SCADUTA);
                    richiestaMedicaRepository.save(richiesta);
                    logger.info("Richiesta {} scaduta automaticamente.", richiesta.getId());
                }
            }
        }
    }
}