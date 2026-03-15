package prenotazione.medica.services;

import com.prenotasalute.commons.service.AbstractGenericService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import prenotazione.medica.dto.ImpegnativaDTO;
import prenotazione.medica.dto.request.ImpegnativaRequest;
import prenotazione.medica.mapper.ImpegnativaMapper;
import prenotazione.medica.model.Impegnativa;
import prenotazione.medica.model.PrestazioneSanitaria;
import prenotazione.medica.model.RichiestaMedica;
import prenotazione.medica.repository.ImpegnativaRepository;
import prenotazione.medica.repository.MedicoCuranteRepository;
import prenotazione.medica.exception.ResourceNotFoundException;
import prenotazione.medica.repository.PazienteRepository;
import prenotazione.medica.services.I18nMessageService;

/**
 * Servizio per Impegnativa: CRUD generico (commons) e generazione da richiesta medica.
 * <p>
 * <b>Ruolo nell'architettura:</b> invocato da {@link prenotazione.medica.controller.ImpegnativaController} (POST genera-impegnativa).
 * Recupera la richiesta medica, la accetta se non già accettata, crea Impegnativa e
 * PrestazioneSanitaria collegate e persiste. Solo il medico curante può generare impegnative.
 * </p>
 */
@Service
public class ImpegnativaService extends AbstractGenericService<Impegnativa, ImpegnativaDTO, Long> {

    private final ImpegnativaRepository impegnativaRepository;
    private final RichiestaMedicaService richiestaMedicaService;
    private final PazienteRepository pazienteRepository;
    private final MedicoCuranteRepository medicoCuranteRepository;
    private final ModelMapper modelMapper;
    private final I18nMessageService i18n;

    public ImpegnativaService(ImpegnativaRepository impegnativaRepository,
                              ImpegnativaMapper impegnativaMapper,
                              RichiestaMedicaService richiestaMedicaService,
                              PazienteRepository pazienteRepository,
                              MedicoCuranteRepository medicoCuranteRepository,
                              ModelMapper modelMapper,
                              I18nMessageService i18n) {
        super(impegnativaRepository, impegnativaMapper);
        this.impegnativaRepository = impegnativaRepository;
        this.richiestaMedicaService = richiestaMedicaService;
        this.pazienteRepository = pazienteRepository;
        this.medicoCuranteRepository = medicoCuranteRepository;
        this.modelMapper = modelMapper;
        this.i18n = i18n;
    }

    @Override
    public ImpegnativaDTO create(ImpegnativaDTO dto) {
        Impegnativa entity = mapper.toEntity(dto);
        if (dto.getIdPaziente() != null) {
            entity.setPaziente(pazienteRepository.getReferenceById(dto.getIdPaziente()));
        }
        if (dto.getIdMedicoCurante() != null) {
            entity.setMedicoCurante(medicoCuranteRepository.getReferenceById(dto.getIdMedicoCurante()));
        }
        Impegnativa saved = impegnativaRepository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    public ImpegnativaDTO update(Long id, ImpegnativaDTO dto) {
        Impegnativa entity = impegnativaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("impegnativa.notfound", id));
        mapper.updateEntityFromDTO(dto, entity);
        if (dto.getIdPaziente() != null) {
            entity.setPaziente(pazienteRepository.getReferenceById(dto.getIdPaziente()));
        }
        if (dto.getIdMedicoCurante() != null) {
            entity.setMedicoCurante(medicoCuranteRepository.getReferenceById(dto.getIdMedicoCurante()));
        }
        Impegnativa saved = impegnativaRepository.save(entity);
        return mapper.toDTO(saved);
    }

    public String generaImpegnativa(ImpegnativaRequest request)
    {
        Impegnativa impegnativa = new Impegnativa();

        PrestazioneSanitaria prestazioneSanitaria = modelMapper.map(request.getPrestazioneSanitariaDTO(), PrestazioneSanitaria.class);
        impegnativa.setPrestazioneSanitaria(prestazioneSanitaria);
        prestazioneSanitaria.setImpegnativa(impegnativa);

        RichiestaMedica richiestaMedica = richiestaMedicaService.accettaRichiestaMedica(request.getIdRichiestaMedica()).get();

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

        return i18n.getMessage("impegnativa.generated");
    }

}