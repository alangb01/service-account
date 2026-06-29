package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class AccountRepositoryException extends RuntimeException {
    public AccountRepositoryException(String message) {
        super(message);
    }

    public AccountRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
