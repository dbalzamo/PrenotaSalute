package prenotazione.medica.shared.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione del bean {@link org.modelmapper.ModelMapper} per la conversione tra entità e DTO.
 * <p>
 * <b>Ruolo nell'architettura:</b> i servizi usano ModelMapper per trasformare entità JPA
 * nei rispettivi DTO di risposta senza scrivere codice di mapping manuale.
 * Il bean è singleton e condiviso in tutta l'applicazione.
 * </p>
 *
 * @see org.modelmapper.ModelMapper – libreria che mappa campi con lo stesso nome tra classi diverse.
 */
@Configuration
public class ModelMapperConfig
{
    /**
     * Espone ModelMapper come bean Spring. Invocato una sola volta alla creazione del contesto.
     */
    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }
}