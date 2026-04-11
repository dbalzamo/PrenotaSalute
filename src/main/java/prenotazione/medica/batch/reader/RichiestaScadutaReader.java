package prenotazione.medica.batch.reader;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;
import prenotazione.medica.richiestaMedica.repository.RichiestaMedicaRepository;
import prenotazione.medica.shared.enums.EStatoRichiesta;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;

/**
 * <strong>Reader</strong> = “chi porta i dati dentro il batch”. Spring Batch chiama questo oggetto molte volte e ogni volta gli chiede:
 * “dammi la prossima {@link RichiestaMedica} da lavorare”. Questa classe non modifica le righe e non decide la regola di scadenza nel dettaglio:
 * <strong>sceglie solo quali righe meritano di entrare in coda</strong> secondo una condizione sul database.
 * <p>
 * <strong>Che cos’è {@link org.springframework.batch.item.ItemReader}.</strong> È un’interfaccia di Spring Batch. L’unico metodo che
 * interessa davvero qui è {@link #read()}. La regola d’oro: finché ci sono dati, {@code read()} restituisce un oggetto;
 * quando i dati sono finiti, deve restituire {@code null}. {@code null} non significa “errore”, significa <strong>“ho finito, non c’è altro”</strong>,
 * un po’ come arrivare in fondo a un file di testo.
 * </p>
 * <p>
 * <strong>Quali richieste vengono lette.</strong> Dal database prendiamo solo le richieste che contemporaneamente:
 * </p>
 * <ul>
 *   <li>hanno stato {@link prenotazione.medica.shared.enums.EStatoRichiesta#ACCETTATA ACCETTATA} (sono state accettate in passato);</li>
 *   <li>hanno una {@code dataAccettazione} <strong>più vecchia</strong> di una data limite calcolata così: prendi l’istante “adesso”
 *       ({@link Instant#now()}), sottrai 30 giorni ({@link ChronoUnit#DAYS}). Se la data di accettazione è prima di quella limite,
 *       la riga entra nell’elenco. In parole povere: “accettata da più di trenta giorni”.</li>
 * </ul>
 * <p>
 * Il “perché 30 giorni” è una regola di dominio del progetto: in un manuale va bene ricordare che quel numero si può cambiare se il business cambia.
 * </p>
 * <p>
 * <strong>Perché esiste {@link org.springframework.batch.core.annotation.BeforeStep @BeforeStep} e il metodo {@link #init}.</strong>
 * Potremmo interrogare il database dentro ogni {@code read()}, ma sarebbe lento e ripetitivo. Invece, <strong>una sola volta</strong>
 * prima di iniziare il ciclo di lettura, Spring Batch chiama {@code init}. Lì eseguiamo la query e mettiamo il risultato in un
 * {@link Iterator} (un oggetto Java che sa darti “il prossimo elemento” fino a esaurimento). Dopo, {@code read()} fa solo
 * {@code iterator.next()} oppure {@code null}: è leggero.
 * </p>
 * <p>
 * <strong>Ruolo del {@link prenotazione.medica.richiestaMedica.repository.RichiestaMedicaRepository}.</strong> È lo strato che parla col database
 * (in questo progetto tramite Spring Data JPA). Noi non scriviamo SQL a mano in questa classe: usiamo un metodo di ricerca già definito sul repository.
 * </p>
 *
 * @see prenotazione.medica.batch.processor.RichiestaScadutaProcessor
 * @see prenotazione.medica.batch.config.BatchConfig
 */
@RequiredArgsConstructor
@Component
public class RichiestaScadutaReader implements ItemReader<RichiestaMedica> {

    private static final Logger log = LoggerFactory.getLogger(RichiestaScadutaReader.class);

    private final RichiestaMedicaRepository repository;
    private Iterator<RichiestaMedica> iterator;

    /**
     * Preparazione <strong>una tantum</strong> prima che Spring Batch inizi a chiamare {@link #read()} in loop.
     * <p>
     * Cosa fa nel dettaglio:
     * </p>
     * <ol>
     *   <li>Calcola {@code limite} = adesso meno 30 giorni.</li>
     *   <li>Chiama il repository: “dammi tutte le richieste ACCETTATE con data di accettazione prima di quel limite”.</li>
     *   <li>Trasforma la lista in un {@link Iterator} e la salva nel campo {@code iterator} della classe.</li>
     * </ol>
     * <p>
     * <strong>Chi chiama {@code init}?</strong> Spring Batch, automaticamente, perché il metodo ha {@code @BeforeStep}.
     * Non compare nel tuo codice applicativo.
     * </p>
     * <p>
     * <strong>A cosa serve {@link StepExecution}.</strong> È un oggetto che descrive “questa esecuzione dello step” (id, nomi, contesto).
     * In molti tutorial avanzati si usa per leggere parametri o salvare contatori. Qui il parametro è obbligatorio per la firma dell’annotazione,
     * ma non lo usiamo: va bene lo stesso; non va rimosso solo perché “non serve”, altrimenti l’API non combacia.
     * </p>
     *
     * @param stepExecution informazioni sull’esecuzione corrente dello step (fornite da Spring Batch)
     */
    @BeforeStep
    public void init(StepExecution stepExecution) {
        Instant limite = Instant.now().minus(30, ChronoUnit.DAYS);
        List<RichiestaMedica> candidates = repository.findByStatoAndDataAccettazioneBefore(EStatoRichiesta.ACCETTATA, limite);
        iterator = candidates.iterator();
        log.info(
                "RichiestaScadutaReader initialized: stepName={} jobExecutionId={} candidateCount={} stato={} acceptanceCutoffInstant={}",
                stepExecution.getStepName(),
                stepExecution.getJobExecutionId(),
                candidates.size(),
                EStatoRichiesta.ACCETTATA,
                limite);
    }

    /**
     * Spring Batch chiama questo metodo ripetutamente. Ogni chiamata deve rispondere: “ecco il prossimo elemento” oppure “ho finito”.
     * <p>
     * Logica riga per riga del codice:
     * </p>
     * <ul>
     *   <li>Se {@code iterator} non è ancora stato creato ({@code null}) oppure non ha un prossimo elemento ({@code hasNext()} falso),
     *       restituisce {@code null} → Spring Batch capisce che la lettura è terminata.</li>
     *   <li>Altrimenti restituisce {@code iterator.next()}, cioè la prossima {@link RichiestaMedica} in coda.</li>
     * </ul>
     * <p>
     * <strong>Ordine rispetto al processor e al writer.</strong> Ogni oggetto restituito qui (non {@code null}) viene passato subito dopo
     * al {@link prenotazione.medica.batch.processor.RichiestaScadutaProcessor}. Quindi l’ordine di lettura è l’ordine di elaborazione.
     * </p>
     *
     * @return la prossima richiesta da elaborare, oppure {@code null} quando non ce ne sono altre (fine del lavoro di lettura)
     */
    @Override
    public RichiestaMedica read() {
        return (iterator != null && iterator.hasNext()) ? iterator.next() : null;
    }
}
