package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class AccountHolderNotFoundException extends RuntimeException {
    public AccountHolderNotFoundException(String message) {
        super(message);
    }
}
