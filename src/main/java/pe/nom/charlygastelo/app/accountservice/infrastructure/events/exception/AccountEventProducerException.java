package pe.nom.charlygastelo.app.accountservice.infrastructure.events.exception;

public class AccountEventProducerException extends RuntimeException {
    public AccountEventProducerException(String message) {
        super(message);
    }
}
