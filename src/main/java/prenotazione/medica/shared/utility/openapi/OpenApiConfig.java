package prenotazione.medica.shared.utility.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Metadati OpenAPI globali (info applicativa, schema JWT Bearer), collocati nel modulo commons condiviso.
 * <p>
 * L'URL del server per Swagger "Try it out" è impostato in {@code application.properties}
 * tramite {@code springdoc.open-api.servers} (include {@code server.servlet.context-path}).
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
