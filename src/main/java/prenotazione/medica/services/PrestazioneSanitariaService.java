package prenotazione.medica.services;

import com.prenotasalute.commons.service.AbstractGenericService;
import org.springframework.stereotype.Service;
import prenotazione.medica.dto.PrestazioneSanitariaDTO;
import prenotazione.medica.mapper.PrestazioneSanitariaMapper;
import prenotazione.medica.model.PrestazioneSanitaria;
import prenotazione.medica.repository.PrestazioneSanitariaRepository;

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
