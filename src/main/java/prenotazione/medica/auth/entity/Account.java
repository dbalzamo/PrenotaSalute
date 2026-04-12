package prenotazione.medica.auth.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.prenotasalute.commons.entity.EntityBase;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import prenotazione.medica.auth.service.UserDetailsServiceImpl;
import prenotazione.medica.paziente.entity.Paziente;
import prenotazione.medica.medico.entity.MedicoCurante;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import prenotazione.medica.shared.enums.ERuolo;


/**
 * Entità JPA che rappresenta un account di accesso al sistema (credenziali e ruolo).
 * <p>
 * <b>Ruolo nell'architettura:</b> ogni utente del sistema (paziente o medico curante) ha un Account
 * che ne definisce username, email, password e ruolo. L'Account è il punto di ingresso per
 * l'autenticazione Spring Security: {@link UserDetailsServiceImpl}
 * carica un Account e lo converte in {@link UserDetailsImpl} per il contesto di sicurezza.
 * Le relazioni {@link #paziente} e {@link #medicoCurante} sono opzionali e mutualmente esclusive
 * in base al ruolo; i messaggi e le richieste mediche fanno riferimento agli id account per
 * identificare mittente/destinatario o proprietario.
 * </p>
 *
 * @see Entity – mappatura JPA sulla tabella {@code account}.
 * @see JsonBackReference – evita riferimenti circolari in serializzazione JSON (Paziente/MedicoCurante).
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account")
@EntityListeners(AuditingEntityListener.class)
public class Account extends EntityBase
{
	@Column(name = "username", length = 50, nullable = false)
	private String username;
	
	// Controllo sulla mail, utilizzare una regex
	@Column(name = "email", length = 50, nullable = false, unique = true)
	private String email;
	
	@Column(name = "password", length = 255, nullable = false)
	private String password;

	/** Ruolo dell'utente (PAZIENTE o MEDICO_CURANTE); determina quali endpoint e dati può vedere. */
	@Enumerated(EnumType.STRING)
	@Column(name = "ruolo", length = 20)
	private ERuolo ruolo;

	/** Profilo paziente associato a questo account (valorizzato solo se ruolo = PAZIENTE). */
	@OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
	@JsonBackReference
	private Paziente paziente;

	/** Profilo medico curante associato (valorizzato solo se ruolo = MEDICO_CURANTE). */
	@OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
	@JsonBackReference
	private MedicoCurante medicoCurante;

	/**
	 * Costruttore per creazione account (registrazione). Id generato dal DB.
	 */
	public Account(String username, String email, String password, ERuolo ruolo){
		this.username=username;
		this.email=email;
		this.password=password;
		this.ruolo=ruolo;
	}
}