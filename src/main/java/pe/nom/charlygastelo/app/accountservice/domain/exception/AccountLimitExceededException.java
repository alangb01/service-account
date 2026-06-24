package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class AccountLimitExceededException extends RuntimeException{
    public AccountLimitExceededException(String message) {
        super(message);
    }

    public AccountLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}