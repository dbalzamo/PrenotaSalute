package prenotazione.medica.prestazioneSanitaria.service;

import prenotazione.medica.shared.utility.service.AbstractGenericService;
import org.springframework.stereotype.Service;
import prenotazione.medica.impegnativa.service.ImpegnativaService;
import prenotazione.medica.prestazioneSanitaria.dto.PrestazioneSanitariaDTO;
import prenotazione.medica.prestazioneSanitaria.repository.PrestazioneSanitariaRepository;
import prenotazione.medica.prestazioneSanitaria.entity.PrestazioneSanitaria;
import prenotazione.medica.prestazioneSanitaria.mapper.PrestazioneSanitariaMapper;

/**
 * Servizio CRUD per PrestazioneSanitaria basato sulla libreria commons.
 * La creazione/aggiornamento nel contesto di un'impegnativa resta in {@link ImpegnativaService}.
 */
@Service
public class PrestazioneSanitariaService extends AbstractGenericService<PrestazioneSanitaria, PrestazioneSanitariaDTO, Long> {

    public PrestazioneSanitariaService(PrestazioneSanitariaRepository repository,
                                       PrestazioneSanitariaMapper mapper) {
        super(repository, mapper);
    }
}
