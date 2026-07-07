package pe.nom.charlygastelo.app.accountservice.domain.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
