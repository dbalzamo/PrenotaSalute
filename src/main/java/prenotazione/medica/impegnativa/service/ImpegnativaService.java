package prenotazione.medica.impegnativa.service;

import prenotazione.medica.shared.utility.service.AbstractGenericService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prenotazione.medica.auth.SecurityUtils;
import prenotazione.medica.impegnativa.dto.ImpegnativaDTO;
import prenotazione.medica.impegnativa.dto.request.ImpegnativaRequest;
import prenotazione.medica.impegnativa.entity.Impegnativa;
import prenotazione.medica.impegnativa.mapper.ImpegnativaMapper;
import prenotazione.medica.impegnativa.pdf.ImpegnativaPdfService;
import prenotazione.medica.impegnativa.repository.ImpegnativaRepository;
import prenotazione.medica.medico.repository.MedicoCuranteRepository;
import prenotazione.medica.medico.service.MedicoCuranteService;
import prenotazione.medica.paziente.repository.PazienteRepository;
import prenotazione.medica.paziente.service.PazienteService;
import prenotazione.medica.prestazioneSanitaria.entity.PrestazioneSanitaria;
import prenotazione.medica.prestazioneSanitaria.mapper.PrestazioneSanitariaMapper;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;
import prenotazione.medica.richiestaMedica.service.RichiestaMedicaService;
import prenotazione.medica.shared.exception.BadRequestException;
import prenotazione.medica.shared.exception.ResourceNotFoundException;
import prenotazione.medica.shared.i18n.I18nMessageService;

/**
 * Servizio per Impegnativa: CRUD generico (commons), generazione da richiesta medica, PDF.
 */
@Service
public class ImpegnativaService extends AbstractGenericService<Impegnativa, ImpegnativaDTO, Long> {

    private final ImpegnativaRepository impegnativaRepository;
    private final RichiestaMedicaService richiestaMedicaService;
    private final PazienteRepository pazienteRepository;
    private final MedicoCuranteRepository medicoCuranteRepository;
    private final PrestazioneSanitariaMapper prestazioneSanitariaMapper;
    private final I18nMessageService i18n;
    private final PazienteService pazienteService;
    private final MedicoCuranteService medicoCuranteService;
    private final ImpegnativaPdfService impegnativaPdfService;

    public ImpegnativaService(ImpegnativaRepository impegnativaRepository,
                              ImpegnativaMapper impegnativaMapper,
                              RichiestaMedicaService richiestaMedicaService,
                              PazienteRepository pazienteRepository,
                              MedicoCuranteRepository medicoCuranteRepository,
                              PrestazioneSanitariaMapper prestazioneSanitariaMapper,
                              I18nMessageService i18n,
                              PazienteService pazienteService,
                              MedicoCuranteService medicoCuranteService,
                              ImpegnativaPdfService impegnativaPdfService) {
        super(impegnativaRepository, impegnativaMapper);
        this.impegnativaRepository = impegnativaRepository;
        this.richiestaMedicaService = richiestaMedicaService;
        this.pazienteRepository = pazienteRepository;
        this.medicoCuranteRepository = medicoCuranteRepository;
        this.prestazioneSanitariaMapper = prestazioneSanitariaMapper;
        this.i18n = i18n;
        this.pazienteService = pazienteService;
        this.medicoCuranteService = medicoCuranteService;
        this.impegnativaPdfService = impegnativaPdfService;
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

    @Transactional
    public String generaImpegnativa(ImpegnativaRequest request) {
        if (impegnativaRepository.findByRichiestaMedica_Id(request.getIdRichiestaMedica()).isPresent()) {
            throw new BadRequestException("impegnativa.exists.for.request");
        }

        Impegnativa impegnativa = new Impegnativa();

        PrestazioneSanitaria prestazioneSanitaria =
                prestazioneSanitariaMapper.toEntity(request.getPrestazioneSanitariaDTO());
        impegnativa.setPrestazioneSanitaria(prestazioneSanitaria);
        prestazioneSanitaria.setImpegnativa(impegnativa);

        RichiestaMedica richiestaMedica = richiestaMedicaService
                .accettaRichiestaMedica(request.getIdRichiestaMedica())
                .orElseThrow(() -> new ResourceNotFoundException("richiesta.notfound", request.getIdRichiestaMedica()));

        impegnativa.setRichiestaMedica(richiestaMedica);
        impegnativa.setRegione(richiestaMedica.getPaziente().getIndirizzoDiResidenza());
        impegnativa.setCodiceNRE("0900A" + System.currentTimeMillis() % 1000000000L);
        impegnativa.setTipoRicetta(richiestaMedica.getTipoRichiesta());
        impegnativa.setPriorità(request.getPriorita());
        impegnativa.setPaziente(richiestaMedica.getPaziente());
        impegnativa.setMedicoCurante(richiestaMedica.getMedicoCurante());

        impegnativaRepository.save(impegnativa);

        return i18n.getMessage("impegnativa.generated");
    }

    /**
     * PDF dell'impegnativa se il richiedente è il paziente o il medico curante titolare.
     */
    @Transactional(readOnly = true)
    public byte[] getPdfForCurrentUser(Long impegnativaId) {
        Impegnativa imp = impegnativaRepository.findWithDetailsById(impegnativaId)
                .orElseThrow(() -> new ResourceNotFoundException("impegnativa.notfound", impegnativaId));

        Long accountId = SecurityUtils.getCurrentAccountId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isPaziente = hasRole(auth, "ROLE_PAZIENTE");
        boolean isMedico = hasRole(auth, "ROLE_MEDICO_CURANTE");

        if (isPaziente) {
            long pazienteId = pazienteService.findByAccountId(accountId).getId();
            if (imp.getPaziente() == null || pazienteId != imp.getPaziente().getId()) {
                throw new ResourceNotFoundException("impegnativa.notfound", impegnativaId);
            }
        } else if (isMedico) {
            long medicoId = medicoCuranteService.findByAccountId(accountId).getId();
            if (imp.getMedicoCurante() == null || medicoId != imp.getMedicoCurante().getId()) {
                throw new ResourceNotFoundException("impegnativa.notfound", impegnativaId);
            }
        } else {
            throw new ResourceNotFoundException("impegnativa.notfound", impegnativaId);
        }

        return impegnativaPdfService.generaPdf(imp);
    }

    private static boolean hasRole(Authentication auth, String role) {
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (role.equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
