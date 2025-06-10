package prenotazione.medica.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_account", referencedColumnName = "id")
    @JsonManagedReference
    private Account account;

    @OneToMany(mappedBy = "paziente")
    @JsonManagedReference
    private List<RichiestaMedica> richiesteMediche;

    @OneToMany(mappedBy = "paziente")
    private List<Impegnativa> impegnative;
}