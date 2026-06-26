package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class CustomerInactiveException extends RuntimeException {
    public CustomerInactiveException(String message) {
        super(message);
    }

    public CustomerInactiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
