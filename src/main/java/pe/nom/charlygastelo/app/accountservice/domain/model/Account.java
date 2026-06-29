package pe.nom.charlygastelo.app.accountservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Account(
        String id,
        String customerId,
        String customerType,
        String number,
        AccountType type,
        BigDecimal balance,
        String currency,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt,
        boolean active,
        AccountStatus status
) {

    public Account withBalance(BigDecimal newBalance) {
        return new Account(
                id,
                customerId,
                customerType,
                number,
                type,
                newBalance,
                currency,
                createdAt,
                LocalDateTime.now(),
                closedAt,
                active,
                status
        );
    }

    public Account updateWith(Account account) {
        return new Account(
                id,
                customerId,
                customerType,
                number,
                account.type() == null ? type : account.type(),
                account.balance() == null ? balance : account.balance(),
                account.currency() == null ? currency : account.currency(),
                createdAt,
                LocalDateTime.now(),
                closedAt,
                active,
                status
        );
    }

    public Account close() {
        return new Account(
                id,
                customerId,
                customerType,
                number,
                type,
                balance,
                currency,
                createdAt,
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                AccountStatus.CLOSED
        );
    }

    public boolean isActive() {
        return active && status == AccountStatus.ACTIVE;
    }


}