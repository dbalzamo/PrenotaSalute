package prenotazione.medica.shared.enums;

/**
 * Ruoli dell'utente nel sistema. Determina permessi e visibilità delle API.
 * <p>
 * Usato in {@code prenotazione.medica.auth.entity.Account#ruolo} e mappato in autorità Spring Security
 * (es. ROLE_PAZIENTE, ROLE_MEDICO_CURANTE) in {@code prenotazione.medica.auth.entity.UserDetailsImpl#build}.
 * Le annotazioni {@code @PreAuthorize} sui controller limitano l'accesso alle API in base al ruolo.
 * </p>
 */
public enum ERuolo {
    SUPER_ADMIN,
    PAZIENTE,
    MEDICO_CURANTE
}

