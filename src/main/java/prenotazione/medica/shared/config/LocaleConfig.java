package prenotazione.medica.shared.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * Configurazione per l'internazionalizzazione (i18n).
 * <p>
 * Locale di default: italiano (IT). Il client può richiedere una lingua tramite header
 * {@code Accept-Language} (es. {@code en} per inglese). In assenza di header viene usato l'italiano.
 * </p>
 */
@Configuration
public class LocaleConfig {

    private static final Locale DEFAULT_LOCALE = Locale.ITALIAN;

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(DEFAULT_LOCALE);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    /**
     * Risolve il locale dalla richiesta HTTP (header Accept-Language).
     * Se assente, usa italiano come default.
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(DEFAULT_LOCALE);
        return resolver;
    }
}
