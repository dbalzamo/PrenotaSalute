package prenotazione.medica.richiestaMedica.service;

import com.prenotasalute.commons.service.AbstractGenericService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import prenotazione.medica.richiestaMedica.dto.response.RichiestaMedicaMedicoResponse;
import prenotazione.medica.richiestaMedica.repository.RichiestaMedicaRepository;
import prenotazione.medica.richiestaMedica.api.RichiestaMedicaController;
import prenotazione.medica.richiestaMedica.dto.RichiestaMedicaDTO;
import prenotazione.medica.richiestaMedica.dto.request.RichiestaMedicaRequest;
import prenotazione.medica.richiestaMedica.dto.request.RifiutoRichiestaRequest;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;
import prenotazione.medica.richiestaMedica.mapper.RichiestaMedicaMapper;
import prenotazione.medica.shared.enums.ERuolo;
import prenotazione.medica.shared.enums.EStatoRichiesta;
import prenotazione.medica.shared.enums.ETipoRichiesta;
import prenotazione.medica.medico.service.MedicoCuranteService;
import prenotazione.medica.auth.entity.Account;
import prenotazione.medica.medico.entity.MedicoCurante;
import prenotazione.medica.paziente.entity.Paziente;
import prenotazione.medica.medico.repository.MedicoCuranteRepository;
import prenotazione.medica.paziente.repository.PazienteRepository;
import prenotazione.medica.paziente.service.PazienteService;
import prenotazione.medica.shared.exception.ResourceNotFoundException;
import prenotazione.medica.richiestaMedica.repository.spec.RichiestaMedicaSpecification;
import prenotazione.medica.auth.SecurityUtils;
import prenotazione.medica.shared.i18n.I18nMessageService;
import prenotazione.medica.auth.service.AccountService;
import prenotazione.medica.impegnativa.entity.Impegnativa;
import prenotazione.medica.impegnativa.repository.ImpegnativaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import prenotazione.medica.richiestaMedica.dto.response.RichiestaMedicaListItemDTO;

/**
 * Servizio per il ciclo di vita delle richieste mediche: CRUD generico (commons), creazione da
 * dashboard, visualizzazione, accettazione, rifiuto e job di scadenza.
 * <p>
 * <b>Ruolo nell'architettura:</b> invocato da {@link RichiestaMedicaController} per tutti gli
 * endpoint /api/v1/richieste-mediche. Gestisce transizioni di stato ({@link EStatoRichiesta}),
 * filtri per paziente/medico e stato, e uno job {@link Scheduled} che marca come SCADUTA le
 * richieste accettate oltre un limite temporale. Fornisce anche i dati per le notifiche in
 * dashboard (conteggi per stato).
 * </p>
 */
@Service
public class RichiestaMedicaService extends AbstractGenericService<RichiestaMedica, RichiestaMedicaDTO, Long> {

    private static final Logger logger = LoggerFactory.getLogger(RichiestaMedicaService.class);

    private final RichiestaMedicaRepository richiestaMedicaRepository;
    private final AccountService accountService;
    private final PazienteService pazienteService;
    private final MedicoCuranteService medicoCuranteService;
    private final PazienteRepository pazienteRepository;
    private final MedicoCuranteRepository medicoCuranteRepository;
    private final ImpegnativaRepository impegnativaRepository;
    private final I18nMessageService i18n;

    public RichiestaMedicaService(RichiestaMedicaRepository richiestaMedicaRepository,
                                  RichiestaMedicaMapper richiestaMedicaMapper,
                                  AccountService accountService,
                                  PazienteService pazienteService,
                                  MedicoCuranteService medicoCuranteService,
                                  PazienteRepository pazienteRepository,
                                  MedicoCuranteRepository medicoCuranteRepository,
                                  ImpegnativaRepository impegnativaRepository,
                                  I18nMessageService i18n) {
        super(richiestaMedicaRepository, richiestaMedicaMapper);
        this.richiestaMedicaRepository = richiestaMedicaRepository;
        this.accountService = accountService;
        this.pazienteService = pazienteService;
        this.medicoCuranteService = medicoCuranteService;
        this.pazienteRepository = pazienteRepository;
        this.medicoCuranteRepository = medicoCuranteRepository;
        this.impegnativaRepository = impegnativaRepository;
        this.i18n = i18n;
    }

    @Override
    public RichiestaMedicaDTO create(RichiestaMedicaDTO dto) {
        RichiestaMedica entity = mapper.toEntity(dto);
        if (dto.getIdPaziente() != null) {
            entity.setPaziente(pazienteRepository.getReferenceById(dto.getIdPaziente()));
        }
        if (dto.getIdMedicoCurante() != null) {
            entity.setMedicoCurante(medicoCuranteRepository.getReferenceById(dto.getIdMedicoCurante()));
        }
        RichiestaMedica saved = richiestaMedicaRepository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    public RichiestaMedicaDTO update(Long id, RichiestaMedicaDTO dto) {
        RichiestaMedica entity = richiestaMedicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("richiesta.notfound", id));
        mapper.updateEntityFromDTO(dto, entity);
        if (dto.getIdPaziente() != null) {
            entity.setPaziente(pazienteRepository.getReferenceById(dto.getIdPaziente()));
        }
        if (dto.getIdMedicoCurante() != null) {
            entity.setMedicoCurante(medicoCuranteRepository.getReferenceById(dto.getIdMedicoCurante()));
        }
        RichiestaMedica saved = richiestaMedicaRepository.save(entity);
        return mapper.toDTO(saved);
    }

    public String creaRichiestaMedica(RichiestaMedicaRequest request)
    {
        RichiestaMedica richiestaMedica = new RichiestaMedica();
        richiestaMedica.setDescrizione(request.getDescrizione());
        richiestaMedica.setId(null);
        richiestaMedica.setPaziente(pazienteService.findByAccountId(SecurityUtils.getCurrentAccountId()));
        richiestaMedica.setMedicoCurante(medicoCuranteService.findById(request.getIdMedico()));
        richiestaMedica.setTipoRichiesta(ETipoRichiesta.valueOf(request.getTipoRichiesta()));
        richiestaMedica.setDataEmissione(new Date());
        richiestaMedica.setStato(EStatoRichiesta.INVIATA);

        richiestaMedicaRepository.save(richiestaMedica);
        return i18n.getMessage("richiesta.created");
    }


    /**
     * Restituisce le richieste filtrate per stato e per l'utente autenticato (paziente o medico).
     * Usa {@link RichiestaMedicaSpecification} per criteri composabili.
     */
    public Page<RichiestaMedicaListItemDTO> findAllByStatoAndPazienteId(EStatoRichiesta stato, int page, int size) {
        Optional<Account> accountOptional = accountService.findById(SecurityUtils.getCurrentAccountId());
        if (accountOptional.isEmpty()) {
            return Page.empty();
        }
        Account accountUtente = accountOptional.get();
        var spec = RichiestaMedicaSpecification.hasStato(stato);
        if (accountUtente.getRuolo() == ERuolo.PAZIENTE) {
            Paziente paziente = pazienteService.findByAccountId(accountUtente.getId());
            spec = spec.and(RichiestaMedicaSpecification.hasPazienteId(paziente.getId()));
        } else if (accountUtente.getRuolo() == ERuolo.MEDICO_CURANTE) {
            MedicoCurante medicoCurante = medicoCuranteService.findByAccountId(accountUtente.getId());
            spec = spec.and(RichiestaMedicaSpecification.hasMedicoCuranteId(medicoCurante.getId()));
        } else {
            return Page.empty();
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataEmissione"));
        Page<RichiestaMedica> pageEntities = richiestaMedicaRepository.findAll(spec, pageable);
        Map<Long, Long> impegnativaByRichiesta = impegnativaIdsByRichiestaIds(
                pageEntities.getContent().stream().map(RichiestaMedica::getId).toList());
        return pageEntities.map(r -> toListItemDto(r, impegnativaByRichiesta));
    }

    /** Restituisce le richieste del paziente attualmente autenticato in forma paginata, ordinate per data (più recente prima). */
    public Page<RichiestaMedicaListItemDTO> findAllByPazienteId(int page, int size) {
        Long accountId = SecurityUtils.getCurrentAccountId();
        Optional<Account> accountOptional = accountService.findById(accountId);
        if (accountOptional.isEmpty()) {
            logger.warn("findAllByPazienteId: account non trovato per id={}", accountId);
            return Page.empty();
        }
        Account account = accountOptional.get();
        if (account.getRuolo() != ERuolo.PAZIENTE) {
            logger.warn("findAllByPazienteId: account id={} non è PAZIENTE (ruolo={})", accountId, account.getRuolo());
            return Page.empty();
        }
        Paziente paziente = pazienteService.findByAccountId(account.getId());
        var spec = RichiestaMedicaSpecification.hasPazienteId(paziente.getId());
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataEmissione"));
        Page<RichiestaMedica> pageEntities = richiestaMedicaRepository.findAll(spec, pageable);
        Map<Long, Long> impegnativaByRichiesta = impegnativaIdsByRichiestaIds(
                pageEntities.getContent().stream().map(RichiestaMedica::getId).toList());
        Page<RichiestaMedicaListItemDTO> result = pageEntities.map(r -> toListItemDto(r, impegnativaByRichiesta));
        logger.info("findAllByPazienteId: accountId={}, pazienteId={}, richieste={}", accountId, paziente.getId(), result.getTotalElements());
        return result;
    }

    private Map<Long, Long> impegnativaIdsByRichiestaIds(List<Long> richiestaIds) {
        if (richiestaIds == null || richiestaIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Impegnativa> list = impegnativaRepository.findByRichiestaMedica_IdIn(richiestaIds);
        Map<Long, Long> map = new HashMap<>();
        for (Impegnativa i : list) {
            if (i.getRichiestaMedica() != null) {
                map.put(i.getRichiestaMedica().getId(), i.getId());
            }
        }
        return map;
    }

    private RichiestaMedicaListItemDTO toListItemDto(RichiestaMedica r, Map<Long, Long> impegnativaByRichiesta) {
        RichiestaMedicaListItemDTO dto = new RichiestaMedicaListItemDTO();
        dto.setId(r.getId());
        dto.setDataEmissione(r.getDataEmissione());
        dto.setDataAccettazione(r.getDataAccettazione());
        dto.setTipoRichiesta(r.getTipoRichiesta() != null ? r.getTipoRichiesta().name() : null);
        dto.setStato(r.getStato() != null ? r.getStato().name() : null);
        dto.setDescrizione(r.getDescrizione());
        dto.setImpegnativaId(impegnativaByRichiesta.get(r.getId()));
        return dto;
    }

    /** Restituisce tutte le richieste del medico curante attualmente autenticato, con dati paziente per la dashboard. */
    public List<RichiestaMedicaMedicoResponse> findAllByMedicoCuranteId() {
        Long accountId = SecurityUtils.getCurrentAccountId();
        Optional<Account> accountOptional = accountService.findById(accountId);
        if (accountOptional.isEmpty() || accountOptional.get().getRuolo() != ERuolo.MEDICO_CURANTE) {
            return Collections.emptyList();
        }
        MedicoCurante medico = medicoCuranteService.findByAccountId(accountOptional.get().getId());
        var spec = RichiestaMedicaSpecification.hasMedicoCuranteId(medico.getId());
        List<RichiestaMedica> list = richiestaMedicaRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "dataEmissione"));
        Map<Long, Long> impegnativaByRichiesta = impegnativaIdsByRichiestaIds(
                list.stream().map(RichiestaMedica::getId).toList());
        List<RichiestaMedicaMedicoResponse> result = new ArrayList<>();
        for (RichiestaMedica r : list) {
            RichiestaMedicaMedicoResponse dto = new RichiestaMedicaMedicoResponse();
            dto.setId(r.getId());
            dto.setDataEmissione(r.getDataEmissione());
            dto.setTipoRichiesta(r.getTipoRichiesta() != null ? r.getTipoRichiesta().name() : null);
            dto.setStato(r.getStato() != null ? r.getStato().name() : null);
            dto.setDescrizione(r.getDescrizione());
            dto.setImpegnativaId(impegnativaByRichiesta.get(r.getId()));
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
            return i18n.getMessage("richiesta.viewed", id);
        }
        throw new ResourceNotFoundException("richiesta.notfound", id);
    }

    public String rifiutaRichiestaMedica(RifiutoRichiestaRequest rifiutoRichiestaRequest)
    {
        Optional<RichiestaMedica> richiestaMedica = richiestaMedicaRepository.findById(rifiutoRichiestaRequest.getIdRichiesta());

        if (richiestaMedica.isPresent()){
            richiestaMedica.get().setStato(EStatoRichiesta.RIFIUTATA);
            richiestaMedica.get().setDescrizione(rifiutoRichiestaRequest.getMotivazione());
            richiestaMedicaRepository.save(richiestaMedica.get());
            return i18n.getMessage("richiesta.refused", rifiutoRichiestaRequest.getIdRichiesta(), rifiutoRichiestaRequest.getMotivazione());
        }
        throw new ResourceNotFoundException("richiesta.notfound", rifiutoRichiestaRequest.getIdRichiesta());
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
        throw new ResourceNotFoundException("richiesta.notfound", idRichiestaMedica);
    }

    public RichiestaMedica marcaScaduta(RichiestaMedica richiestaMedica){
        richiestaMedica.setStato(EStatoRichiesta.SCADUTA);
        return richiestaMedica;
    }
}