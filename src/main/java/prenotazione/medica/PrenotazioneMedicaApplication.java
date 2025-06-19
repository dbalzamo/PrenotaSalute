package prenotazione.medica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PrenotazioneMedicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrenotazioneMedicaApplication.class, args);
	}
	
}