package prenotazione.medica.shared.config;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import prenotazione.medica.shared.utility.entity.EntityBase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.security.core.context.SecurityContext;
import prenotazione.medica.auth.SecurityUtils;

/**
 * Configurazione Spring Data JPA Auditing per {@link EntityBase}
 * e per {@link prenotazione.medica.auth.entity.Account} (anch'esso estende {@code EntityBase}).
 * <p>
 * <b>Timestamp:</b> {@link #utcAuditingDateTimeProvider} imposta {@code createdAt}/{@code updatedAt}
 * in UTC, allineato a {@code spring.jpa.properties.hibernate.jdbc.time_zone=UTC}.
 * <b>Utente corrente:</b> {@link #springSecurityAuditorAware} espone l'id account quando è disponibile
 * il {@link SecurityContext}; con {@code Optional.empty()}
 * per richieste anonime o job senza autenticazione (signup, {@code @Scheduled}, ecc.).
 * Quando la commons aggiungerà {@code @CreatedBy}/{@code LastModifiedBy} su {@code EntityBase},
 * non sarà necessario modificare questa configurazione.
 * </p>
 */
@Configuration
@EnableJpaAuditing(
        auditorAwareRef = "springSecurityAuditorAware",
        dateTimeProviderRef = "utcAuditingDateTimeProvider"
)
public class JpaAuditingConfig {

    /**
     * Fornisce l'istante corrente in UTC per i campi {@code LocalDateTime} gestiti da auditing.
     */
    @Bean(name = "utcAuditingDateTimeProvider")
    public DateTimeProvider utcAuditingDateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Fornisce l'id dell'account autenticato per eventuali campi {@code @CreatedBy} / {@code @LastModifiedBy}.
     */
    @Bean(name = "springSecurityAuditorAware")
    public AuditorAware<Long> springSecurityAuditorAware() {
        return SecurityUtils::getCurrentAccountIdOptional;
    }
}
