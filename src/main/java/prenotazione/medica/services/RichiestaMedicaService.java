package prenotazione.medica.services;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import prenotazione.medica.dto.request.RichiestaMedicaRequest;
import prenotazione.medica.dto.request.RifiutoRichiestaRequest;
import prenotazione.medica.dto.response.RichiestaMedicaMedicoResponse;
import prenotazione.medica.enums.ERuolo;
import prenotazione.medica.enums.EStatoRichiesta;
import prenotazione.medica.enums.ETipoRichiesta;
import prenotazione.medica.model.Account;
import prenotazione.medica.model.MedicoCurante;
import prenotazione.medica.model.Paziente;
import prenotazione.medica.model.RichiestaMedica;
import prenotazione.medica.repository.RichiestaMedicaRepository;
import prenotazione.medica.security.utils.SecurityUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Servizio per il ciclo di vita delle richieste mediche: creazione, visualizzazione, accettazione,
 * rifiuto e job di scadenza.
 * <p>
 * <b>Ruolo nell'architettura:</b> invocato da {@link prenotazione.medica.controller.RichiestaMedicaController} per tutti gli
 * endpoint /api/richieste-mediche. Gestisce transizioni di stato ({@link EStatoRichiesta}),
 * filtri per paziente/medico e stato, e uno job {@link Scheduled} che marca come SCADUTA le
 * richieste accettate oltre un limite temporale. Fornisce anche i dati per le notifiche in
 * dashboard (conteggi per stato).
 * </p>
 */
@Service
public class RichiestaMedicaService
{
    private static final Logger logger = LoggerFactory.getLogger(RichiestaMedicaService.class);

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


    public String creaRichiestaMedica(RichiestaMedicaRequest request)
    {
        RichiestaMedica richiestaMedica = modelMapper.map(request, RichiestaMedica.class);

        richiestaMedica.setId(null);
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
            Account accountUtente = accountOptional.get();

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

    /** Restituisce tutte le richieste del paziente attualmente autenticato, ordinate per data (più recente prima). */
    public List<RichiestaMedica> findAllByPazienteId()
    {
        Long accountId = SecurityUtils.getCurrentAccountId();
        Optional<Account> accountOptional = accountService.findById(accountId);
        if (accountOptional.isEmpty()) {
            logger.warn("findAllByPazienteId: account non trovato per id={}", accountId);
            return Collections.emptyList();
        }
        Account account = accountOptional.get();
        if (account.getRuolo() != ERuolo.PAZIENTE) {
            logger.warn("findAllByPazienteId: account id={} non è PAZIENTE (ruolo={})", accountId, account.getRuolo());
            return Collections.emptyList();
        }
        Paziente paziente = pazienteService.findByAccountId(account.getId());
        List<RichiestaMedica> list = richiestaMedicaRepository.findAllByPaziente_IdOrderByDataEmissioneDesc(paziente.getId());
        logger.info("findAllByPazienteId: accountId={}, pazienteId={}, richieste={}", accountId, paziente.getId(), list.size());
        return list;
    }

    /** Restituisce tutte le richieste del medico curante attualmente autenticato, con dati paziente per la dashboard. */
    public List<RichiestaMedicaMedicoResponse> findAllByMedicoCuranteId()
    {
        Long accountId = SecurityUtils.getCurrentAccountId();
        Optional<Account> accountOptional = accountService.findById(accountId);
        if (accountOptional.isEmpty() || accountOptional.get().getRuolo() != ERuolo.MEDICO_CURANTE)
            return Collections.emptyList();
        MedicoCurante medico = medicoCuranteService.findByAccountId(accountOptional.get().getId());
        List<RichiestaMedica> list = richiestaMedicaRepository.findAllByMedicoCurante_IdOrderByDataEmissioneDesc(medico.getId());
        List<RichiestaMedicaMedicoResponse> result = new ArrayList<>();
        for (RichiestaMedica r : list) {
            RichiestaMedicaMedicoResponse dto = new RichiestaMedicaMedicoResponse();
            dto.setId(r.getId());
            dto.setDataEmissione(r.getDataEmissione());
            dto.setTipoRichiesta(r.getTipoRichiesta() != null ? r.getTipoRichiesta().name() : null);
            dto.setStato(r.getStato() != null ? r.getStato().name() : null);
            dto.setDescrizione(r.getDescrizione());
            if (r.getPaziente() != null) {
                dto.setPazienteId(r.getPaziente().getId());
                dto.setPazienteNome(r.getPaziente().getNome());
                dto.setPazienteCognome(r.getPaziente().getCognome());
            }
            result.add(dto);
        }
        return result;
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
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void aggiornaStatiScaduti() {

        Instant limite = Instant.now().minus(30, ChronoUnit.DAYS);

        int aggiornate = richiestaMedicaRepository.scadutaRichieste(limite);

        logger.info("{} richieste scadute automaticamente.", aggiornate);
    }
}