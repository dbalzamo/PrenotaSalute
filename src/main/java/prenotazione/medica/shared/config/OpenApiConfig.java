package prenotazione.medica.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione centralizzata di OpenAPI/Swagger per PrenotaSalute.
 * <p>
 * Definisce:
 * <ul>
 *   <li>metadati dell'API (titolo, descrizione, versione);</li>
 *   <li>server di riferimento (es. ambiente dev locale);</li>
 *   <li>schema di sicurezza JWT Bearer da usare sugli endpoint protetti.</li>
 * </ul>
 * La UI è disponibile in dev/test su /swagger-ui.html.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "PrenotaSalute - API Backend",
                description = "API REST per gestione prenotazioni, pazienti, medici e impegnative della piattaforma PrenotaSalute.",
                version = "v1",
                contact = @Contact(
                        name = "PrenotaSalute Team",
                        email = "support@prenotasalute.example"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Ambiente di sviluppo locale")
        },
        security = {
                @SecurityRequirement(name = "bearer-jwt")
        }
)
@SecurityScheme(
        name = "bearer-jwt",
        description = "JWT Bearer token. Inserire il token come: 'Bearer &lt;JWT&gt;'.",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}

