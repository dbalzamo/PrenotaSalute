package prenotazione.medica.shared.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import prenotazione.medica.shared.security.AuthEntryPointJwt;
import prenotazione.medica.auth.jwt.JwtAuthenticationFilter;
import prenotazione.medica.auth.service.UserDetailsServiceImpl;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

/**
 * Configurazione della sicurezza HTTP (Spring Security).
 * <p>
 * <b>Ruolo nell'architettura:</b> definisce quali path sono pubblici (/api/auth/**, /ws/**), quali
 * richiedono autenticazione e come viene gestita: sessione STATELESS, filtro JWT che legge il token
 * (header Bearer, cookie o query param per WebSocket) e popola il SecurityContext, entry point per
 * risposte 401 in JSON. Abilita anche {@link EnableMethodSecurity} per @PreAuthorize sui controller.
 * </p>
 *
 * @see SecurityFilterChain – catena di filtri applicata a ogni richiesta HTTP.
 * @see DaoAuthenticationProvider – usa UserDetailsService e PasswordEncoder per login form (se usato).
 * @see JwtAuthenticationFilter – estrae e valida il JWT, imposta Authentication nel SecurityContext.
 * @see AuthEntryPointJwt – invocato quando un utente non autenticato accede a una risorsa protetta.
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

	@Value("${server.servlet.context-path:}")
	private String contextPath;

	private final UserDetailsServiceImpl userDetailsService;
	private final AuthEntryPointJwt unauthorizedHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public WebSecurityConfig(UserDetailsServiceImpl userDetailsService,
							 AuthEntryPointJwt unauthorizedHandler,
							 JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.userDetailsService = userDetailsService;
		this.unauthorizedHandler = unauthorizedHandler;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}


	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// Path può arrivare con o senza context-path a seconda del dispatcher
		String apiAuth = contextPath + "/api/auth/**";
		String apiV1 = contextPath + "/api/v1/**";

		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth ->
						auth.requestMatchers("/error", contextPath + "/error").permitAll()
								.requestMatchers("/api/auth/**", apiAuth).permitAll()
								// SpringDoc / Swagger UI (path con e senza context-path)
								.requestMatchers(
										"/swagger-ui.html", contextPath + "/swagger-ui.html",
										"/swagger-ui/**", contextPath + "/swagger-ui/**",
										"/v3/api-docs", contextPath + "/v3/api-docs",
										"/v3/api-docs/**", contextPath + "/v3/api-docs/**"
								).permitAll()
								.requestMatchers("/api/v1/**", apiV1).authenticated()
								.anyRequest().authenticated());
		http.authenticationProvider(authenticationProvider());
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(
				"http://localhost:4200",  // Angular dev
				"http://localhost:80",    // Angular in Docker
				"http://localhost"        // nginx senza porta
		));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}