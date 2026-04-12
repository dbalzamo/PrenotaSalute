package prenotazione.medica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto di ingresso dell'applicazione Spring Boot "Prenotazione Medica".
 * <p>
 * <b>Ruolo nell'architettura:</b> avvia il contesto Spring, abilita la scansione dei componenti
 * (controller, service, repository, config) e abilita lo scheduling per eventuali task periodici.
 * Tutte le altre classi del progetto sono gestite dal contesto Spring creato qui.
 * </p>
 *
 * @SpringBootApplication combina: configurazione, auto-configurazione e component scan del package.
 * @EnableScheduling permette l'uso di @Scheduled per job ricorrenti (se presenti).
 * JPA Auditing è attivato in {@link prenotazione.medica.shared.config.JpaAuditingConfig}.
 */
@SpringBootApplication
@EnableScheduling
public class PrenotazioneMedicaApplication {

	/**
	 * Avvia il server embedded (Tomcat) e il contesto Spring.
	 * Invocato solo al lancio dell'applicazione (es. {@code mvn spring-boot:run}).
	 */
	public static void main(String[] args) {
		SpringApplication.run(PrenotazioneMedicaApplication.class, args);
	}
	
}