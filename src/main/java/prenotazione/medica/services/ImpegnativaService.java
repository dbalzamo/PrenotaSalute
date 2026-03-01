package prenotazione.medica.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import prenotazione.medica.dto.request.ImpegnativaRequest;
import prenotazione.medica.dto.response.ImpegnativaResponse;
import prenotazione.medica.enums.EStatoRichiesta;
import prenotazione.medica.model.Impegnativa;
import prenotazione.medica.model.Paziente;
import prenotazione.medica.model.PrestazioneSanitaria;
import prenotazione.medica.model.RichiestaMedica;
import prenotazione.medica.repository.ImpegnativaRepository;

import java.util.Date;

/**
 * Servizio per la generazione di impegnative a partire da richieste mediche accettate.
 * <p>
 * <b>Ruolo nell'architettura:</b> invocato da {@link prenotazione.medica.controller.ImpegnativaController} (POST genera-impegnativa).
 * Recupera la richiesta medica, la accetta se non già accettata, crea Impegnativa e
 * PrestazioneSanitaria collegate e persiste. Solo il medico curante può generare impegnative.
 * </p>
 */
@Service
public class ImpegnativaService
{
    @Autowired
    private ImpegnativaRepository impegnativaRepository;
    @Autowired
    private RichiestaMedicaService richiestaMedicaService;
    @Autowired
    private PazienteService pazienteService;
    @Autowired
    private ModelMapper modelMapper;


    public String generaImpegnativa(ImpegnativaRequest request)
    {
        Impegnativa impegnativa = new Impegnativa();

        PrestazioneSanitaria prestazioneSanitaria = modelMapper.map(request.getPrestazioneSanitariaDTO(), PrestazioneSanitaria.class);
        impegnativa.setPrestazioneSanitaria(prestazioneSanitaria);
        prestazioneSanitaria.setImpegnativa(impegnativa);

        RichiestaMedica richiestaMedica = richiestaMedicaService.accettaRichiestaMedica(request.getIdRichiestaMedica()).get();

        // COMPILO I DATI DELL'IMPEGNATIVA
        impegnativa.setRegione(richiestaMedica.getPaziente().getIndirizzoDiResidenza());
        impegnativa.setCodiceNRE("0900A" + System.currentTimeMillis() % 1000000000L);
        impegnativa.setTipoRicetta(richiestaMedica.getTipoRichiesta());
        impegnativa.setPriorità(request.getPriorita());
        impegnativa.setPaziente(richiestaMedica.getPaziente());
        impegnativa.setMedicoCurante(richiestaMedica.getMedicoCurante());

        prestazioneSanitaria.setCodicePrestazione(request.getPrestazioneSanitariaDTO().getCodicePrestazione());
        prestazioneSanitaria.setDescrizione(request.getPrestazioneSanitariaDTO().getDescrizione());
        prestazioneSanitaria.setNote(request.getPrestazioneSanitariaDTO().getNote());
        prestazioneSanitaria.setQuantita(request.getPrestazioneSanitariaDTO().getQuantita());

        impegnativaRepository.save(impegnativa);

        return "Impegnativa generata con successo!";
    }

}