package prenotazione.medica.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Questa classe risponde alla domanda: <strong>“Quando parte il lavoro batch?”</strong> Non legge il database e non scrive righe:
 * è solo una <em>sveglia</em> che, a orari prefissati, dice a Spring Batch “adesso esegui il job”.
 * <p>
 * <strong>Differenza importante da ricordare.</strong> Spring Batch (reader / processor / writer) è una cosa.
 * Lo <strong>scheduling</strong> (annotazione {@code @Scheduled}) è un’altra: fa parte di Spring normale e serve a eseguire un metodo
 * ripetuto nel tempo, come una sveglia che suona ogni giorno. Questa classe unisce le due cose: la sveglia chiama il lanciatore del batch.
 * </p>
 * <p>
 * <strong>Cosa sono {@link JobLauncher} e {@link Job}.</strong> Il {@code JobLauncher} è il pulsante “play”: gli passi il job da eseguire
 * e i parametri, e lui avvia tutta la catena configurata in {@link prenotazione.medica.batch.config.BatchConfig}.
 * Il {@code Job} qui è proprio {@code aggiornaStatiScadutoJob}, cioè il lavoro “aggiorna le richieste scadute”.
 * Spring inietta questi oggetti nel costruttore (grazie a Lombok {@code @RequiredArgsConstructor} e {@code @Component}):
 * non devi creare nulla a mano con {@code new}.
 * </p>
 * <p>
 * <strong>Cosa succede quando scatta la sveglia.</strong> Il metodo {@link #esegui()} viene chiamato da Spring sul thread del pianificatore.
 * Dentro costruisce {@link JobParameters}: una busta con coppie nome/valore. Qui aggiungiamo {@code timestamp} con l’ora attuale in millisecondi.
 * Serve perché Spring Batch, senza parametri che cambiano, potrebbe pensare che stai rilanciando la stessa identica esecuzione:
 * il timestamp rende ogni lancio “diverso” e quindi valido.
 * </p>
 * <p>
 * <strong>Cron {@code 0 0 0 1 * ?} spiegato campo per campo.</strong> La stringa cron dice al pianificatore quando eseguire.
 * I campi (sintassi tipo Quartz, usata da Spring) sono: secondo, minuto, ora, giorno del mese, mese, giorno della settimana.
 * {@code 0 0 0} = alle 00:00:00 (mezzanotte in punto: il primo {@code 0} sono i secondi, il secondo i minuti, il terzo le ore, tutti a zero).
 * {@code 1} = il giorno 1 del mese.
 * {@code *} = ogni mese.
 * {@code ?} = “non mi interessa il giorno della settimana” (si usa quando si fissa il giorno del mese).
 * Quindi: <strong>ogni primo giorno del mese, a mezzanotte</strong> (nel fuso orario del server JVM: va tenuto presente in produzione).
 * </p>
 *
 * @see prenotazione.medica.batch.config.BatchConfig
 * @see org.springframework.scheduling.annotation.Scheduled
 */
@RequiredArgsConstructor
@Component
public class BatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(BatchScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job aggiornaStatiScadutoJob;

    /**
     * Metodo che <strong>mette in moto</strong> tutto il batch una volta. Passi dopo passo:
     * <ol>
     *   <li>Si crea un {@link JobParametersBuilder}, che serve a costruire i parametri del job.</li>
     *   <li>Si aggiunge un numero lungo chiamato {@code timestamp} con {@link System#currentTimeMillis()}: sono i millisecondi trascorsi
     *       da una data fissa nel passato fino ad “adesso”. Ogni volta che chiami il metodo, quel numero è diverso.</li>
     *   <li>{@code toJobParameters()} chiude la costruzione e dà un oggetto {@link JobParameters} immutabile.</li>
     *   <li>{@code jobLauncher.run(aggiornaStatiScadutoJob, params)} chiede a Spring Batch di eseguire il job con quei parametri.
     *       Da qui in poi entra in scena lo step con reader, processor e writer.</li>
     * </ol>
     * <p>
     * <strong>Chi chiama {@code esegui()}?</strong> Non un controller o un utente: Spring, automaticamente, quando il cron lo decide.
     * Tu non devi scrivere {@code esegui()} nel tuo codice HTTP.
     * </p>
     * <p>
     * <strong>Perché {@code throws Exception}?</strong> {@link JobLauncher#run(Job, JobParameters)} dichiara di poter lanciare eccezioni checked.
     * Il metodo le lascia salire: Spring Scheduling le gestirà secondo la configurazione (in produzione conviene loggare o monitorare).
     * </p>
     *
     * @throws Exception se il lancio o l’esecuzione del job falliscono (requisito della firma di {@code JobLauncher.run})
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void esegui() throws Exception {
        long ts = System.currentTimeMillis();
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", ts)
                .toJobParameters();
        String jobName = aggiornaStatiScadutoJob.getName();
        log.info("Scheduled batch launch starting: jobName={} parameterTimestamp={}", jobName, ts);
        try {
            JobExecution execution = jobLauncher.run(aggiornaStatiScadutoJob, params);
            log.info(
                    "Scheduled batch launch completed: jobName={} executionId={} status={} exitStatus={} durationMs={}",
                    jobName,
                    execution.getId(),
                    execution.getStatus(),
                    execution.getExitStatus(),
                    execution.getEndTime() != null && execution.getStartTime() != null
                            ? Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis()
                            : null);
        } catch (Exception e) {
            log.error("Scheduled batch launch failed: jobName={} parameterTimestamp={}", jobName, ts, e);
            throw e;
        }
    }
}
