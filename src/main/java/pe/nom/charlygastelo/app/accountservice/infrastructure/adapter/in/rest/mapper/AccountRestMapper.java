package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.AccountResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.CreateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.UpdateAccountRequest;

@Component
public class AccountRestMapper {

    public Account toDomain(CreateAccountRequest request) {

        return new Account(
                null,
                request.customerId(),
                request.customerType(),
                request.number(),
                AccountType.valueOf(request.type()),
                BigDecimal.ZERO,
                request.currency(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                true,
                AccountStatus.ACTIVE
        );
    }

    public Account toDomain(UpdateAccountRequest request) {

        return new Account(
                null,
                request.customerId(),
                request.type(),
                request.number(),
                AccountType.valueOf(request.type()),
                request.balance(),
                request.currency(),
                null,
                request.updatedAt() == null
                        ? null
                        : LocalDateTime.ofInstant(
                        Instant.parse(request.updatedAt()),
                        ZoneId.systemDefault()
                ),
                null,
                request.active(),
                AccountStatus.valueOf(request.status())
        );
    }

    public AccountResponse toResponse(Account account) {

        return new AccountResponse(
                account.id(),
                account.customerId(),
                account.customerType(),
                account.number(),
                account.type().name(),
                account.balance(),
                account.currency(),
                account.createdAt(),
                account.updatedAt(),
                account.closedAt(),
                account.active(),
                account.status().name()
        );
    }

}