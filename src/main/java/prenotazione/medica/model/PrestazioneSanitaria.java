package prenotazione.medica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prestazione_sanitaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestazioneSanitaria
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "codice_prestazione", unique = true, nullable = false)
    private Long codicePrestazione;

    @Column(name = "descrizione", length = 255)
    private String descrizione;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "quantita", nullable = false)
    private int quantita;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_impegnativa", referencedColumnName = "id")
    private Impegnativa impegnativa;
}