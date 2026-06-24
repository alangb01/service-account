package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class CreditProductDebtOverdueException extends RuntimeException {
    public CreditProductDebtOverdueException(String message) {
        super(message);
    }
}
