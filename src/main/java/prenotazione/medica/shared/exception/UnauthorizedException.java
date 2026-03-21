package prenotazione.medica.shared.exception;

/**
 * Eccezione per accesso non autorizzato (HTTP 401).
 * Il gestore globale risolve il messaggio tramite i18n usando la chiave e gli argomenti.
 */
public class UnauthorizedException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public UnauthorizedException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args != null ? args : new Object[0];
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}
