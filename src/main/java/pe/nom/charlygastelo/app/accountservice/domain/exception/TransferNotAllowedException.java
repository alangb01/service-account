package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class TransferNotAllowedException extends RuntimeException {
    public TransferNotAllowedException(String message) {
        super(message);
    }
}
