package prenotazione.medica.medico.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.prenotasalute.commons.entity.EntityBase;
import jakarta.persistence.*;
import prenotazione.medica.medico.repository.MedicoCuranteRepository;
import prenotazione.medica.medico.service.MedicoCuranteService;
import prenotazione.medica.paziente.entity.Paziente;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import prenotazione.medica.auth.entity.Account;
import prenotazione.medica.impegnativa.entity.Impegnativa;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;

import java.util.Date;
import java.util.List;



/**
 * Entità JPA che rappresenta un medico curante (dati anagrafici e relazioni).
 * <p>
 * <b>Ruolo nell'architettura:</b> il MedicoCurante è il profilo professionale collegato a un
 * {@link Account}. Gestisce richieste mediche ({@link RichiestaMedica}), emette impegnative
 * ({@link Impegnativa}) e può avere molti {@link Paziente} associati (campo
 * {@link Paziente medicoCurante}); la messaggistica usa gli id degli Account (medico/paziente)
 * per mittente e destinatario. I controller “medico” usano
 * {@link MedicoCuranteRepository} e
 * {@link MedicoCuranteService} per recuperare dati e liste pazienti.
 * </p>
 *
 * @see JsonIgnoreProperties – ignora proxy Hibernate in serializzazione JSON per evitare errori.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "medico_curante")
public class MedicoCurante extends EntityBase {

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

    @Column(name = "specializzazione")
    private String specializzazione;

    /** Account di login del medico (uno-a-uno; nessun cascade sull'account). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_account", referencedColumnName = "id")
    @JsonManagedReference
    private Account account;

    /** Richieste mediche assegnate a questo medico. */
    @OneToMany(mappedBy = "medicoCurante")
    @JsonManagedReference
    private List<RichiestaMedica> richiesteMediche;

    /** Impegnative emesse da questo medico. */
    @OneToMany(mappedBy = "medicoCurante")
    private List<Impegnativa> impegnative;
}
