package prenotazione.medica.security.config;

import org.springframework.beans.factory.annotation.Autowired;
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
import prenotazione.medica.security.jwt.AuthEntryPointJwt;
import prenotazione.medica.security.jwt.JwtAuthenticationFilter;
import prenotazione.medica.services.UserDetailsServiceImpl;

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

	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;


	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// Path può arrivare con o senza context-path a seconda del dispatcher
		String apiAuth = contextPath + "/api/auth/**";
		String apiRichieste = contextPath + "/api/richieste-mediche/**";
		String apiMedico = contextPath + "/api/medico/**";
		http.csrf(csrf -> csrf.disable())
				.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth ->
						auth.requestMatchers("/error", contextPath + "/error").permitAll()
								.requestMatchers("/api/auth/**", apiAuth).permitAll()
								.requestMatchers(contextPath + "/ws/**", "/ws/**").permitAll()
								.requestMatchers("/api/richieste-mediche/**", apiRichieste).authenticated()
								.requestMatchers("/api/medico/**", apiMedico).authenticated()
								.anyRequest().authenticated());
		http.authenticationProvider(authenticationProvider());
		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public JwtAuthenticationFilter authenticationJwtTokenFilter() {
		return new JwtAuthenticationFilter();
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