package prenotazione.medica.impegnativa.service;

import com.prenotasalute.commons.service.AbstractGenericService;
import org.springframework.stereotype.Service;
import prenotazione.medica.impegnativa.dto.ImpegnativaDTO;
import prenotazione.medica.impegnativa.mapper.ImpegnativaMapper;
import prenotazione.medica.impegnativa.repository.ImpegnativaRepository;
import prenotazione.medica.impegnativa.dto.request.ImpegnativaRequest;
import prenotazione.medica.impegnativa.api.ImpegnativaController;
import prenotazione.medica.impegnativa.entity.Impegnativa;
import prenotazione.medica.prestazioneSanitaria.mapper.PrestazioneSanitariaMapper;
import prenotazione.medica.prestazioneSanitaria.entity.PrestazioneSanitaria;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;
import prenotazione.medica.medico.repository.MedicoCuranteRepository;
import prenotazione.medica.richiestaMedica.service.RichiestaMedicaService;
import prenotazione.medica.shared.exception.ResourceNotFoundException;
import prenotazione.medica.paziente.repository.PazienteRepository;
import prenotazione.medica.shared.i18n.I18nMessageService;

/**
 * Servizio per Impegnativa: CRUD generico (commons) e generazione da richiesta medica.
 * <p>
 * <b>Ruolo nell'architettura:</b> invocato da {@link ImpegnativaController} (POST genera-impegnativa).
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
    private final PrestazioneSanitariaMapper prestazioneSanitariaMapper;
    private final I18nMessageService i18n;

    public ImpegnativaService(ImpegnativaRepository impegnativaRepository,
                              ImpegnativaMapper impegnativaMapper,
                              RichiestaMedicaService richiestaMedicaService,
                              PazienteRepository pazienteRepository,
                              MedicoCuranteRepository medicoCuranteRepository,
                              PrestazioneSanitariaMapper prestazioneSanitariaMapper,
                              I18nMessageService i18n) {
        super(impegnativaRepository, impegnativaMapper);
        this.impegnativaRepository = impegnativaRepository;
        this.richiestaMedicaService = richiestaMedicaService;
        this.pazienteRepository = pazienteRepository;
        this.medicoCuranteRepository = medicoCuranteRepository;
        this.prestazioneSanitariaMapper = prestazioneSanitariaMapper;
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

        PrestazioneSanitaria prestazioneSanitaria =
                prestazioneSanitariaMapper.toEntity(request.getPrestazioneSanitariaDTO());
        impegnativa.setPrestazioneSanitaria(prestazioneSanitaria);
        prestazioneSanitaria.setImpegnativa(impegnativa);

        RichiestaMedica richiestaMedica = richiestaMedicaService.accettaRichiestaMedica(request.getIdRichiestaMedica()).get();

        impegnativa.setRegione(richiestaMedica.getPaziente().getIndirizzoDiResidenza());
        impegnativa.setCodiceNRE("0900A" + System.currentTimeMillis() % 1000000000L);
        impegnativa.setTipoRicetta(richiestaMedica.getTipoRichiesta());
        impegnativa.setPriorità(request.getPriorita());
        impegnativa.setPaziente(richiestaMedica.getPaziente());
        impegnativa.setMedicoCurante(richiestaMedica.getMedicoCurante());

        impegnativaRepository.save(impegnativa);

        return i18n.getMessage("impegnativa.generated");
    }

}