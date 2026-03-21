package prenotazione.medica.shared.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Servizio per l'accesso ai messaggi internazionalizzati.
 * <p>
 * Utilizza il locale dalla richiesta corrente ({@link LocaleContextHolder}) o quello di default (italiano).
 * I messaggi sono definiti in {@code messages_it.properties} e {@code messages_en.properties}.
 * </p>
 */
@Service
public class I18nMessageService {

    private final MessageSource messageSource;

    public I18nMessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Restituisce il messaggio per la chiave data, nel locale corrente.
     *
     * @param code chiave del messaggio (es. {@code auth.signup.success})
     * @return messaggio tradotto
     */
    public String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    /**
     * Restituisce il messaggio per la chiave data, con argomenti per sostituzione (es. {0}, {1}).
     *
     * @param code chiave del messaggio
     * @param args argomenti per il placeholder (MessageFormat)
     * @return messaggio tradotto con argomenti sostituiti
     */
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * Restituisce il messaggio per il locale esplicito (es. per contesti senza richiesta HTTP).
     *
     * @param code   chiave del messaggio
     * @param locale locale da usare
     * @return messaggio tradotto
     */
    public String getMessage(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }

    public String getMessage(String code, Object[] args, Locale locale) {
        return messageSource.getMessage(code, args, locale);
    }
}
