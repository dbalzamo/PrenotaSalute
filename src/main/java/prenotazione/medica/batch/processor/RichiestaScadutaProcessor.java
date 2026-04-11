package prenotazione.medica.batch.processor;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;
import prenotazione.medica.richiestaMedica.service.RichiestaMedicaService;

/**
 * <strong>Processor</strong> = “chi applica la regola su ogni singola riga”. Il reader ha solo scelto <em>quali</em> richieste guardare;
 * questa classe dice cosa farne: in questo caso, segnarle come <strong>scadute</strong> dal punto di vista del dominio applicativo.
 * <p>
 * <strong>Dove sta nella catena.</strong> Ordine fisso Spring Batch: prima il reader, poi il processor, poi (a gruppi) il writer.
 * Immagina una catena di montaggio: il reader mette il pezzo sul nastro, il processor lo lavora, il writer imballa molti pezzi insieme.
 * </p>
 * <p>
 * <strong>Perché usiamo {@link RichiestaMedicaService} e non il repository direttamente.</strong> Il service è dove mettiamo le regole
 * “di business” riusabili (anche l’API REST potrebbe chiamare gli stessi metodi). Il batch non deve duplicare la logica:
 * chiama {@code marcaScaduta} così, se domani la regola cambia, la cambi in un posto solo.
 * </p>
 * <p>
 * <strong>Cosa significa restituire un valore oppure {@code null} in un processor.</strong> Spring Batch usa il valore di ritorno così:
 * se restituisci un oggetto (qui una {@link RichiestaMedica}), quell’oggetto entra nel “cestino” che diventerà un chunk per il writer.
 * Se restituisci {@code null}, dici: <strong>“questa riga non deve andare al writer”</strong> (la scarti, come buttare un pezzo difettoso).
 * In <em>questo</em> progetto restituiamo l’entità aggiornata, così il {@link prenotazione.medica.batch.writer.RichiestaScadutaWriter}
 * riceve proprio le righe da salvare e chiama {@code saveAll}: è il modo più chiaro per un manuale, perché vedi tutto il percorso fino al database.
 * </p>
 *
 * @see prenotazione.medica.batch.reader.RichiestaScadutaReader
 * @see prenotazione.medica.batch.writer.RichiestaScadutaWriter
 */
@RequiredArgsConstructor
@Component
public class RichiestaScadutaProcessor implements ItemProcessor<RichiestaMedica, RichiestaMedica> {

    private static final Logger log = LoggerFactory.getLogger(RichiestaScadutaProcessor.class);

    private final RichiestaMedicaService service;

    /**
     * Spring Batch chiama questo metodo una volta per ogni richiesta che il reader ha appena fornito (finché il reader non restituisce {@code null}).
     * <p>
     * Cosa succede dentro:
     * </p>
     * <ol>
     *   <li>Ricevi {@code richiesta}: è un oggetto Java che rappresenta una riga (o meglio: un’entità JPA) già caricata dal database.</li>
     *   <li>Chiami {@link RichiestaMedicaService#marcaScaduta(RichiestaMedica)}. Quel metodo, nel service, imposta lo stato della richiesta
     *       a {@link prenotazione.medica.shared.enums.EStatoRichiesta#SCADUTA SCADUTA} (nel codice del service vedi il dettaglio dei campi toccati).</li>
     *   <li>Restituisci <strong>lo stesso oggetto</strong> che il service ti ha restituito. Non è un oggetto nuovo copiato: è lo stesso riferimento
     *       in memoria, con i campi aggiornati. Spring Batch lo metterà nel chunk e lo passerà al writer.</li>
     * </ol>
     * <p>
     * <strong>Nota sul termine “istanza”.</strong> In Java, quando diciamo “stessa istanza”, intendiamo lo stesso oggetto in memoria,
     * identificato dal riferimento. Se modifichi un campo su quell’oggetto, chi ha lo stesso riferimento vede la modifica.
     * </p>
     *
     * @param richiesta la richiesta appena letta; in condizioni normali era nello stato {@code ACCETTATA} perché il reader filtra così
     * @return la stessa richiesta dopo l’aggiornamento dello stato; Spring Batch la manda al writer dentro un chunk
     */
    @Override
    public RichiestaMedica process(RichiestaMedica richiesta) {
        RichiestaMedica updated = service.marcaScaduta(richiesta);
        log.debug("Richiesta marked expired in batch processor: richiestaMedicaId={} newStato={}", updated.getId(), updated.getStato());
        return updated;
    }
}
