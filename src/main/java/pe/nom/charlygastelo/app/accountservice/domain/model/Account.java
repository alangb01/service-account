package pe.nom.charlygastelo.app.accountservice.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Account(
        String id,
        String customerId,
        String number,
        AccountType type,
        BigDecimal balance,
        String currency,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt,
        boolean active,
        AccountStatus status
) {

    public Account withBalance(BigDecimal newBalance) {
        return new Account(
                id,
                customerId,
                number,
                type,
                newBalance,
                currency,
                createdAt,
                Instant.now(),
                closedAt,
                active,
                status
        );
    }

    public Account updateWith(Account account) {
        return new Account(
                id,
                customerId,
                number,
                account.type() == null ? type : account.type(),
                account.balance() == null ? balance : account.balance(),
                account.currency() == null ? currency : account.currency(),
                createdAt,
                Instant.now(),
                closedAt,
                active,
                status
        );
    }

    public Account createWith(Account account) {
        return new Account(
                id,
                customerId,
                number,
                account.type() == null ? type : account.type(),
                BigDecimal.ZERO,
                account.currency() == null ? currency : account.currency(),
                Instant.now(),
               null,
                null,
                true,
                AccountStatus.ACTIVE
        );
    }

    public Account close() {
        return new Account(
                id,
                customerId,
                number,
                type,
                balance,
                currency,
                createdAt,
                Instant.now(),
                Instant.now(),
                false,
                AccountStatus.CLOSED
        );
    }

    public boolean isActive() {
        return active && status == AccountStatus.ACTIVE;
    }


}