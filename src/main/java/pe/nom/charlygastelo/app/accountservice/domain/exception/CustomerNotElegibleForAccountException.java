package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class CustomerNotElegibleForAccountException extends RuntimeException {
    public CustomerNotElegibleForAccountException(String message) {
        super(message);
    }
}
