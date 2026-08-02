package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class AccountBusinessException extends RuntimeException{
    public AccountBusinessException(String message) {
        super(message);
    }

    public AccountBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}