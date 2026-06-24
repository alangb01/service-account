package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountLimitExceededException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerInactiveException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerNotFoundException;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CUSTOMER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(CustomerInactiveException.class)
    public ResponseEntity<ErrorResponse> handleCustomerInactive(CustomerInactiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CUSTOMER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DOCUMENT_ALREADY_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(AccountLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleDocumentNotFound(AccountLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DOCUMENT_ALREADY_EXISTS", ex.getMessage()));
    }
}
