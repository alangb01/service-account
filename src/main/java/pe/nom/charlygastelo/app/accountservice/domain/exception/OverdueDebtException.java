package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class OverdueDebtException extends RuntimeException {
    public OverdueDebtException(String message) {
        super(message);
    }
}
