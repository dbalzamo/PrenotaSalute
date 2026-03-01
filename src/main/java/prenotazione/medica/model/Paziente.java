package prenotazione.medica.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Entità JPA che rappresenta un paziente nel sistema (dati anagrafici e relazioni).
 * <p>
 * <b>Ruolo nell'architettura:</b> il Paziente è il profilo “clinico” collegato a un
 * {@link Account} (login). Possiede richieste mediche ({@link RichiestaMedica}), impegnative
 * ({@link Impegnativa}) e può essere associato a un {@link MedicoCurante} per messaggistica e
 * contesto di cura. I controller che espongono dati del “paziente loggato” risalgono dall’account
 * al paziente tramite {@link prenotazione.medica.repository.PazienteRepository}.
 * </p>
 *
 * @see Entity – mappatura sulla tabella {@code paziente}.
 * @see JsonManagedReference / JsonBackReference – gestione serializzazione JSON senza cicli.
 */
@Entity
@Table(name = "paziente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paziente
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "nome", length = 20, nullable = false)
    private String nome;

    @Column(name = "cognome", length = 30, nullable = false)
    private String cognome;

    @Column(name = "indirizzo_di_residenza", length = 100, nullable = false)
    private String indirizzoDiResidenza;

    @Column(name = "data_di_nascita", nullable = false)
    private Date dataDiNascita;

    @Column(name = "codice_fiscale", length = 16, nullable = false, unique = true)
    private String codiceFiscale;

    /** Relazione uno-a-uno con l'account di login. */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_account", referencedColumnName = "id")
    @JsonManagedReference
    private Account account;

    /** Richieste mediche inviate da questo paziente. */
    @OneToMany(mappedBy = "paziente")
    @JsonManagedReference
    private List<RichiestaMedica> richiesteMediche;

    /** Impegnative emesse per questo paziente. */
    @OneToMany(mappedBy = "paziente")
    private List<Impegnativa> impegnative;

    /**
     * Medico curante associato al paziente (opzionale).
     * Usato per messaggistica (Posta) e per filtrare richieste/contesto clinico.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_curante_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MedicoCurante medicoCurante;
}