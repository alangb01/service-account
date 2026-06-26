package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.client.HttpClientErrorException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.*;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.ErrorResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.clients.exception.CustomerServiceUnavailableException;

import javax.smartcardio.CardException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // -----------------------------
    // 404 - NOT FOUND
    // -----------------------------
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("CUSTOMER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("ACCOUNT_NOT_FOUND", ex.getMessage()));
    }

    // -----------------------------
    // 400 - BAD REQUEST
    // -----------------------------
    @ExceptionHandler(AccountLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleLimitExceeded(AccountLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("ACCOUNT_LIMIT_EXCEEDED", ex.getMessage()));
    }

    @ExceptionHandler(AccountBusinessException.class)
    public ResponseEntity<ErrorResponse> handleAccountBusinessException(AccountBusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("ACCOUNT_BUSINESS_RESTRICTION", ex.getMessage()));
    }

    // -----------------------------
    // 409 - CONFLICT
    // -----------------------------
    @ExceptionHandler(CustomerInactiveException.class)
    public ResponseEntity<ErrorResponse> handleCustomerInactive(CustomerInactiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CUSTOMER_INACTIVE", ex.getMessage()));
    }

    // -----------------------------
    // 401 - UNAUTHORIZED
    // -----------------------------
    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(HttpClientErrorException.Unauthorized ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("UNAUTHORIZED", ex.getMessage()));
    }

    // -----------------------------
    // 503 - SERVICE UNAVAILABLE
    // -----------------------------
    @ExceptionHandler(CustomerServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleCustomerServiceUnavailable(CustomerServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("CUSTOMER_SERVICE_UNAVAILABLE", ex.getMessage()));
    }

    // -----------------------------
    // 500 - INTERNAL SERVER ERROR (GENÉRICO)
    // -----------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", ex.getMessage()));
    }
}
