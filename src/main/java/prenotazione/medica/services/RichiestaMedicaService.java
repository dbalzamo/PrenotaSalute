package prenotazione.medica.services;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import prenotazione.medica.controller.AuthController;
import prenotazione.medica.dto.request.RichiestaMedicaRequest;
import prenotazione.medica.enums.ERuolo;
import prenotazione.medica.enums.EStatoRichiesta;
import prenotazione.medica.enums.ETipoRichiesta;
import prenotazione.medica.model.Account;
import prenotazione.medica.model.MedicoCurante;
import prenotazione.medica.model.Paziente;
import prenotazione.medica.model.RichiestaMedica;
import prenotazione.medica.repository.RichiestaMedicaRepository;
import prenotazione.medica.security.utils.SecurityUtils;

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
                logger.info("INFO: PAZIENTE CONFRONTO FATTO");
                Paziente paziente = pazienteService.findByAccountId(accountUtente.getId());
                return richiestaMedicaRepository.findAllByStatoAndPaziente_Id(stato, paziente.getId());
            }

            if (accountUtente.getRuolo() == ERuolo.MEDICO_CURANTE)
            {
                logger.info("INFO: MEDICO CONFRONTO FATTO");
                MedicoCurante medicoCurante = medicoCuranteService.findByAccountId(accountUtente.getId());
                return richiestaMedicaRepository.findAllByStatoAndMedicoCurante_Id(stato, medicoCurante.getId());
            }
        }

        return Collections.emptyList();
    }

}