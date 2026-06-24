package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class BusinessAccountException extends RuntimeException{
    public BusinessAccountException(String message) {
        super(message);
    }

    public BusinessAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}