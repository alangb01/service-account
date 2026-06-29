package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class AccountEventProducerException extends RuntimeException {
    public AccountEventProducerException(String message) {
        super(message);
    }
}
