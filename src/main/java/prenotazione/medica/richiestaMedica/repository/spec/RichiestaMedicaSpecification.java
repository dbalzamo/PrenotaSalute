package prenotazione.medica.richiestaMedica.repository.spec;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import prenotazione.medica.richiestaMedica.repository.RichiestaMedicaRepository;
import prenotazione.medica.shared.enums.EStatoRichiesta;
import prenotazione.medica.shared.enums.ETipoRichiesta;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;

import java.time.Instant;
import java.util.Date;

/**
 * Specification JPA per query dinamiche su {@link RichiestaMedica}.
 * <p>
 * Permette di comporre criteri di filtro (stato, paziente, medico, tipo, date) in modo
 * type-safe e riutilizzabile. Usato da {@link RichiestaMedicaRepository}
 * tramite {@link org.springframework.data.jpa.repository.JpaSpecificationExecutor}.
 * </p>
 *
 * @see org.springframework.data.jpa.domain.Specification
 */
public final class RichiestaMedicaSpecification {

    private RichiestaMedicaSpecification() {
    }

    /**
     * Filtra per stato della richiesta.
     *
     * @param stato stato richiesto (non null)
     * @return specification per stato
     */
    public static Specification<RichiestaMedica> hasStato(EStatoRichiesta stato) {
        return (root, query, cb) -> stato == null ? cb.conjunction() : cb.equal(root.get("stato"), stato);
    }

    /**
     * Filtra per id del paziente.
     *
     * @param pazienteId id del paziente (non null)
     * @return specification per paziente
     */
    public static Specification<RichiestaMedica> hasPazienteId(Long pazienteId) {
        return (root, query, cb) -> pazienteId == null ? cb.conjunction()
                : cb.equal(root.get("paziente").get("id"), pazienteId);
    }

    /**
     * Filtra per id del medico curante.
     *
     * @param medicoCuranteId id del medico curante (non null)
     * @return specification per medico curante
     */
    public static Specification<RichiestaMedica> hasMedicoCuranteId(Long medicoCuranteId) {
        return (root, query, cb) -> medicoCuranteId == null ? cb.conjunction()
                : cb.equal(root.get("medicoCurante").get("id"), medicoCuranteId);
    }

    /**
     * Filtra per tipo di richiesta.
     *
     * @param tipo tipo richiesta (può essere null, in tal caso nessun filtro)
     * @return specification per tipo
     */
    public static Specification<RichiestaMedica> hasTipoRichiesta(ETipoRichiesta tipo) {
        return (root, query, cb) -> tipo == null ? cb.conjunction() : cb.equal(root.get("tipoRichiesta"), tipo);
    }

    /**
     * Filtra richieste con data di emissione nell'intervallo [from, to].
     *
     * @param from inizio intervallo (incluso); null = nessun limite inferiore
     * @param to   fine intervallo (inclusa); null = nessun limite superiore
     * @return specification per intervallo data emissione
     */
    public static Specification<RichiestaMedica> dataEmissioneBetween(Date from, Date to) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (from != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("dataEmissione"), from));
            }
            if (to != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("dataEmissione"), to));
            }
            return p;
        };
    }

    /**
     * Filtra richieste con data di accettazione precedente o uguale a {@code before}.
     * Utile per il job di scadenza (richieste accettate oltre un certo limite).
     *
     * @param before limite temporale (non null)
     * @return specification per data accettazione
     */
    public static Specification<RichiestaMedica> dataAccettazioneBefore(Instant before) {
        return (root, query, cb) -> before == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("dataAccettazione"), Date.from(before));
    }
}
