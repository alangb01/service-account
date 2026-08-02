package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class CustomerHasOverdueDebtException extends RuntimeException {

    public CustomerHasOverdueDebtException(String customerId) {
        super("Customer has overdue debt: " + customerId);
    }
}