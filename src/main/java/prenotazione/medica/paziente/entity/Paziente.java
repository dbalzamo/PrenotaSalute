package prenotazione.medica.paziente.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import prenotazione.medica.shared.utility.entity.EntityBase;
import prenotazione.medica.medico.entity.MedicoCurante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import prenotazione.medica.auth.entity.Account;
import prenotazione.medica.impegnativa.entity.Impegnativa;
import prenotazione.medica.paziente.repository.PazienteRepository;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;

import java.util.Date;
import java.util.List;

/**
 * Entità JPA che rappresenta un paziente nel sistema (dati anagrafici e relazioni).
 * <p>
 * <b>Ruolo nell'architettura:</b> il Paziente è il profilo “clinico” collegato a un
 * {@link Account} (login). Possiede richieste mediche ({@link RichiestaMedica}), impegnative
 * ({@link Impegnativa}) e può essere associato a un {@link MedicoCurante} per messaggistica e
 * contesto di cura. I controller che espongono dati del “paziente loggato” risalgono dall’account
 * al paziente tramite {@link PazienteRepository}.
 * </p>
 *
 * @see Entity – mappatura sulla tabella {@code paziente}.
 * @see JsonManagedReference / JsonBackReference – gestione serializzazione JSON senza cicli.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "paziente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paziente extends EntityBase {

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

    /** Relazione uno-a-uno con l'account di login (nessun cascade: l'account è persistito da {@link AccountService}). */
    @OneToOne(fetch = FetchType.LAZY)
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

