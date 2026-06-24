package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class DebitCardNotLinkedException extends RuntimeException {
    public DebitCardNotLinkedException(String message) {
        super(message);
    }
}
