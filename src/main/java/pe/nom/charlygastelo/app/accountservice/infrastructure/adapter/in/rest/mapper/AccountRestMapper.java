package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.AccountResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.CloseAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.CreateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.UpdateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.clients.dto.CustomerResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class AccountRestMapper {


    public Account toDomain (CreateAccountRequest request){
        return new Account(
                null,
                request.customerId(),
                request.number(),
                AccountType.valueOf(request.type()),
                BigDecimal.ZERO,
                request.currency(),
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );
    }

    public Account toDomain (UpdateAccountRequest request){
        return  new Account(
                null,
                request.customerId(),
                request.number(),
                AccountType.valueOf(request.type()),
                request.balance(),
                request.currency(),
                null,
                LocalDateTime.ofInstant(Instant.parse(request.updatedAt()), ZoneId.systemDefault()),
                null,
                request.active(),
                request.status()
        );
    }

    public Account toDomain (CloseAccountRequest request){
        return  new Account(
                null,
                request.customerId(),
                null,
               null,
                null,
                null,
                null,
                null,
                LocalDateTime.ofInstant(Instant.parse(request.closedAt()), ZoneId.systemDefault()),
                request.active(),
                "CLOSED"
        );
    }

    public AccountResponse toResponse(Account a) {
        return new AccountResponse(
                a.id(),
                a.customerId(),
                a.number(),
                a.type().toString(),
                a.balance(),
                a.currency(),
                a.createdAt(),
                a.active(),
                a.status()
        );
    }

}
