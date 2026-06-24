package pe.nom.charlygastelo.app.accountservice.infrastructure.clients.exception;

public class CustomerServiceUnavailableException extends RuntimeException {
    public CustomerServiceUnavailableException(String message) {
        super(message);
    }
}
