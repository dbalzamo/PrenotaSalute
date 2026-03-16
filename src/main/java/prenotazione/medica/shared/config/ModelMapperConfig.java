package prenotazione.medica.shared.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import prenotazione.medica.chat.entity.Message;
import prenotazione.medica.chat.dto.response.MessageResponse;
import prenotazione.medica.chat.service.MessageService;

/**
 * Configurazione del bean {@link org.modelmapper.ModelMapper} per la conversione tra entità e DTO.
 * <p>
 * <b>Ruolo nell'architettura:</b> i servizi (es. {@link MessageService})
 * usano ModelMapper per trasformare entità JPA (es. {@link Message}) in DTO
 * di risposta (es. {@link MessageResponse}) senza scrivere codice
 * di mapping manuale. Il bean è singleton e condiviso in tutta l'applicazione.
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