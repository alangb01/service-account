package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class AccountSignerNotFoundException extends RuntimeException {
    public AccountSignerNotFoundException(String message) {
        super(message);
    }
}
