package prenotazione.medica.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import prenotazione.medica.batch.processor.RichiestaScadutaProcessor;
import prenotazione.medica.batch.reader.RichiestaScadutaReader;
import prenotazione.medica.batch.writer.RichiestaScadutaWriter;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;

/**
 * Questa classe serve solo a <strong>dire a Spring come assemblare i pezzi del batch</strong>. Non contiene regole di business
 * (non decide chi è scaduto e cosa scrivere nel database): collega tra loro reader, processor e writer come fossero mattoncini LEGO.
 * <p>
 * <b>Che cos’è Spring Batch (spiegato da zero).</b> Immagina di avere mille righe nel database da controllare. Farle tutte in un colpo solo
 * può essere pesante per la memoria e rischioso se qualcosa va storto a metà. Spring Batch è una libreria di Spring che organizza il lavoro
 * a <strong>piccoli gruppi</strong> chiamati <em>chunk</em> (in italiano: “pezzo”, “blocco”). Per ogni blocco fa sempre la stessa sequenza:
 * </p>
 * <ol>
 *   <li><strong>Reader</strong> ({@link org.springframework.batch.item.ItemReader}): “dammi la prossima riga da lavorare”, una alla volta.</li>
 *   <li><strong>Processor</strong> ({@link org.springframework.batch.item.ItemProcessor}): “leggi quella riga e applichi la regola”
 *       (qui: segna la richiesta come scaduta). Può anche buttare via la riga restituendo il valore speciale {@code null}.</li>
 *   <li><strong>Writer</strong> ({@link org.springframework.batch.item.ItemWriter}): “salva sul database un <em>gruppo</em> di righe già elaborate”,
 *       così non vai al database una volta per ogni singola riga.</li>
 * </ol>
 * <p>
 * <strong>Job</strong> e <strong>step</strong> sono nomi Spring Batch. Il <em>job</em> è il “lavoro totale” (es. “aggiorna tutte le richieste scadute”).
 * Lo <em>step</em> è un pezzo di quel lavoro; qui ce n’è uno solo, con nome {@code aggiornaStatiScadutoStep}.
 * Il {@link org.springframework.batch.core.repository.JobRepository JobRepository} è come un <strong>diario</strong> dove Spring Batch annota
 * cosa è successo (utile se il programma si riavvia e vuoi capire dove eri rimasto: concetto avanzato, basta sapere che esiste).
 * </p>
 * <p>
 * <b>Ordine delle cose quando il batch parte (passo dopo passo):</b>
 * </p>
 * <ol>
 *   <li>Qualcuno deve <strong>avviare</strong> il job. In questo progetto lo fa il {@link prenotazione.medica.batch.scheduler.BatchScheduler}
 *       usando il {@link org.springframework.batch.core.launch.JobLauncher JobLauncher} (componente che dice “vai, esegui questo job”).</li>
 *   <li>Spring Batch entra nello step e ripete: chiama il reader → chiama il processor → se il processor non ha restituito {@code null},
 *       mette il risultato in un cestino. Quando nel cestino ci sono abbastanza elementi (qui: 50), chiama il writer per salvarli,
 *       poi svuota il cestino e ricomincia.</li>
 *   <li>Quando il reader non ha più righe (restituisce {@code null}), lo step finisce e il job finisce.</li>
 * </ol>
 * <p>
 * Le annotazioni {@code @Configuration} e {@code @Bean} sono di Spring: dicono “questi oggetti esistono nell’applicazione e altri possono usarli”.
 * </p>
 *
 * @see prenotazione.medica.batch.reader.RichiestaScadutaReader
 * @see prenotazione.medica.batch.processor.RichiestaScadutaProcessor
 * @see prenotazione.medica.batch.writer.RichiestaScadutaWriter
 */
@Configuration
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    /**
     * Crea lo <strong>step</strong> Spring Batch chiamato {@code aggiornaStatiScadutoStep}. Uno step è una fase del lavoro:
     * qui l’unica fase è “leggi dal database, elabora, scrivi sul database a gruppi”.
     * <p>
     * Il numero {@code 50} dentro {@code chunk(50, transactionManager)} è la <strong>dimensione del blocco</strong>.
     * Significa: “quando hai raccolto 50 richieste che il processor ha accettato (cioè ha restituito un oggetto diverso da {@code null}),
     * chiama il writer una volta e salva quel gruppo”. Perché 50 e non 1? Per andare meno volte al database: è un compromesso tra memoria e velocità.
     * </p>
     * <p>
     * {@link PlatformTransactionManager} è la parte di Spring che gestisce la <strong>transazione</strong>: in parole semplici,
     * “o tutte le scritture di quel blocco hanno successo, oppure nessuna viene lasciata a metà” (come annullare una mossa se qualcosa va male).
     * </p>
     * <p>
     * <strong>Chi chiama questo metodo?</strong> Non lo chiami tu nel codice normale. Spring, all’avvio dell’applicazione, vede {@code @Bean},
     * costruisce lo step e lo tiene pronto. Quando parte il job, Spring Batch usa questo step automaticamente.
     * Reader, processor e writer sono classi separate marcate {@code @Component}: Spring le crea e le “infila” qui perché servono allo step.
     * </p>
     * <p>
     * Le due occorrenze di {@link RichiestaMedica} in {@code .&lt;RichiestaMedica, RichiestaMedica&gt;chunk} significano:
     * “il reader dà una {@code RichiestaMedica} in ingresso al processor, e il processor restituisce ancora una {@code RichiestaMedica} in uscita
     * verso il writer”. Se un giorno trasformassi i dati in un altro tipo, cambieresti il secondo tipo.
     * </p>
     *
     * @param jobRepository      il “diario” di Spring Batch ({@link org.springframework.batch.core.repository.JobRepository}) per tracciare esecuzioni
     * @param transactionManager gestisce transazioni database per ogni blocco di 50 elementi
     * @param reader             oggetto che legge le righe una alla volta dal database
     * @param processor          oggetto che applica la regola su ogni riga letta
     * @param writer             oggetto che salva un gruppo di righe sul database
     * @return lo step assemblato, pronto per essere messo dentro un job
     */
    @Bean
    public Step aggiornaStatiScadutoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                         RichiestaScadutaReader reader, RichiestaScadutaProcessor processor, RichiestaScadutaWriter writer) {
        Step step = new StepBuilder("aggiornaStatiScadutoStep", jobRepository)
                .<RichiestaMedica, RichiestaMedica>chunk(50, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
        log.debug("Spring Batch step registered: stepName={} chunkSize={} itemType={}",
                step.getName(),
                50,
                RichiestaMedica.class.getSimpleName());
        return step;
    }

    /**
     * Crea il <strong>job</strong> Spring Batch chiamato {@code aggiornaStatiScadutoJob}. Il job è il “contenitore” del lavoro:
     * dice da quale step partire e in che ordine eseguire gli step. Qui il flusso è facilissimo: c’è un solo step e parte subito
     * ({@code .start(aggiornaStatiScadutoStep)}).
     * <p>
     * <strong>Perché il nome è importante.</strong> Spring Batch usa stringhe come {@code aggiornaStatiScadutoJob} per riconoscere questo lavoro
     * nel {@link JobRepository}. È come l’etichetta su una cartella.
     * </p>
     * <p>
     * <strong>JobParameters (parametri del job).</strong> Ogni volta che lanci il job puoi passare dei parametri (numeri, stringhe…).
     * Spring Batch considera “nuova” un’esecuzione se i parametri cambiano. Il {@link prenotazione.medica.batch.scheduler.BatchScheduler}
     * aggiunge un timestamp proprio per questo: così due lanci il primo del mese non vengono scambiati per la stessa identica corsa.
     * </p>
     * <p>
     * <strong>Chi usa questo bean?</strong> Il {@link org.springframework.batch.core.launch.JobLauncher} quando qualcuno chiama
     * {@code run(aggiornaStatiScadutoJob, parametri)} — nel nostro caso lo scheduler.
     * </p>
     *
     * @param jobRepository            diario / registro delle esecuzioni Spring Batch
     * @param aggiornaStatiScadutoStep lo step unico che fa tutto il lavoro di lettura-elaborazione-scrittura
     * @return il job pronto da eseguire
     */
    @Bean
    public Job aggiornaStatiScadutoJob(JobRepository jobRepository, Step aggiornaStatiScadutoStep) {
        Job job = new JobBuilder("aggiornaStatiScadutoJob", jobRepository)
                .start(aggiornaStatiScadutoStep)
                .build();
        log.debug("Spring Batch job registered: jobName={} firstStep={}", job.getName(), aggiornaStatiScadutoStep.getName());
        return job;
    }
}
