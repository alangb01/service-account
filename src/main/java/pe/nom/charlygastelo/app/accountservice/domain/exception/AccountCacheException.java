package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class AccountCacheException extends RuntimeException {
    public AccountCacheException(String message) {
        super(message);
    }

    public AccountCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
