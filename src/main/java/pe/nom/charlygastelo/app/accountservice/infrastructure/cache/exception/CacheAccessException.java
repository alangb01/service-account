package pe.nom.charlygastelo.app.accountservice.infrastructure.cache.exception;

public class CacheAccessException extends RuntimeException {
    public CacheAccessException(String message) {
        super(message);
    }
}
