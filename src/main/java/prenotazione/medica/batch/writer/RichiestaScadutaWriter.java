package prenotazione.medica.batch.writer;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;
import prenotazione.medica.richiestaMedica.repository.RichiestaMedicaRepository;

import java.util.List;

/**
 * <strong>Writer</strong> = “chi scrive sul database a gruppi”. Il reader dà <strong>una</strong> richiesta alla volta; il writer, invece,
 * riceve <strong>molte richieste insieme</strong> dentro un contenitore chiamato {@link Chunk}. È una scelta di efficienza: parlare col database
 * una volta ogni 50 righe di solito costa meno che farlo 50 volte da sole.
 * <p>
 * <strong>Che cos’è un {@link Chunk}.</strong> È una classe di Spring Batch che avvolge una lista di elementi già passati dal processor.
 * Tu non costruisci il chunk a mano nel codice di business: lo prepara Spring Batch quando il blocco è pieno (nel nostro caso, fino a 50 elementi,
 * come scritto in {@link prenotazione.medica.batch.config.BatchConfig}) oppure quando i dati stanno finendo e deve svuotare l’ultimo blocco parziale.
 * </p>
 * <p>
 * <strong>Quali elementi finiscono nel chunk.</strong> Solo quelli per cui il processor ha restituito un valore <strong>diverso da {@code null}</strong>.
 * Se il processor restituisse {@code null} per una riga, quella riga non comparirebbe in questo writer per quella esecuzione.
 * Qui il {@link prenotazione.medica.batch.processor.RichiestaScadutaProcessor} restituisce sempre l’entità aggiornata dopo {@code marcaScaduta},
 * quindi le richieste lette e processate tendono ad arrivare tutte fin qui.
 * </p>
 * <p>
 * <strong>Cosa fa {@code saveAll} sul {@link prenotazione.medica.richiestaMedica.repository.RichiestaMedicaRepository}.</strong>
 * È un metodo tipico di Spring Data JPA: chiede al framework di salvare (inserire o aggiornare) tutte le entità della lista.
 * In pratica, dopo questa chiamata, le modifiche fatte in memoria (stato {@code SCADUTA}) diventano persistenti nel database
 * (salvo errori di transazione gestiti da Spring più in alto).
 * </p>
 * <p>
 * <strong>JPA in una riga.</strong> JPA è lo strato che mappa oggetti Java ↔ tabelle SQL; il “repository” è l’interfaccia che usi per non scrivere SQL a mano.
 * </p>
 *
 * @see org.springframework.batch.item.ItemWriter
 */
@RequiredArgsConstructor
@Component
public class RichiestaScadutaWriter implements ItemWriter<RichiestaMedica> {

    private static final Logger log = LoggerFactory.getLogger(RichiestaScadutaWriter.class);

    private final RichiestaMedicaRepository repository;

    /**
     * Spring Batch chiama questo metodo ogni volta che deve <strong>svuotare</strong> un chunk sul database.
     * <p>
     * Passi concettuali:
     * </p>
     * <ol>
     *   <li>Ricevi {@code chunk}: non è “la singola richiesta”, è la busta con dentro una lista.</li>
     *   <li>{@code chunk.getItems()} ti dà la lista Java delle {@link RichiestaMedica} da salvare.</li>
     *   <li>{@code repository.saveAll(...)} invia quella lista a Spring Data JPA perché vengano scritte nel database.</li>
     * </ol>
     * <p>
     * <strong>Chi chiama {@code write}?</strong> Il motore Spring Batch, dentro lo step configurato in {@link prenotazione.medica.batch.config.BatchConfig}.
     * Tu non lo invochi da un controller.
     * </p>
     * <p>
     * <strong>Lista vuota.</strong> In teoria il chunk potrebbe essere vuoto in alcuni scenari avanzati; {@code saveAll} su lista vuota
     * di solito non fa danni, ma non salva nulla. Per imparare, l’idea importante è: writer = punto in cui le modifiche diventano “ufficiali” sul DB
     * in questo progetto.
     * </p>
     *
     * @param chunk il blocco di richieste elaborate che Spring Batch chiede di persistere; usa {@link Chunk#getItems()} per ottenere la lista
     */
    @Override
    public void write(Chunk<? extends RichiestaMedica> chunk) {
        List<? extends RichiestaMedica> items = chunk.getItems();
        if (items.isEmpty()) {
            log.debug("RichiestaScadutaWriter skipping persist: empty chunk");
            return;
        }
        log.info("RichiestaScadutaWriter persisting chunk: itemCount={}", items.size());
        repository.saveAll(items);
    }
}
