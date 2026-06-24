package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class TransactionLimitExceededException extends RuntimeException {
    public TransactionLimitExceededException(String message) {
        super(message);
    }
}
