package prenotazione.medica.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import lombok.*;

import prenotazione.medica.enums.ERuolo;


@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "username", length = 50, nullable = false)
	private String username;
	
	// Controllo sulla mail, utilizzare una regex
	@Column(name = "email", length = 50, nullable = false, unique = true)
	private String email;
	
	@Column(name = "password", length = 255, nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "ruolo", length = 20)
	private ERuolo ruolo;

	@OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
	@JsonBackReference
	private Paziente paziente;

	@OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
	@JsonBackReference
	private MedicoCurante medicoCurante;

	public Account(String username, String email, String password, ERuolo ruolo){
		this.username=username;
		this.email=email;
		this.password=password;
		this.ruolo=ruolo;
	}
}