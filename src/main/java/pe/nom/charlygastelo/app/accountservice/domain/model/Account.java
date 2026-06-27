package pe.nom.charlygastelo.app.accountservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record Account(
        String id,
        String customerId,
        String number,
        AccountType type,
        BigDecimal balance,
        String currency,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt,
        boolean active,
        String status
) {

    // Actualiza solo los campos no nulos del otro Account
    public Account updateWith(Account other) {
        return new Account(
                other.id != null ? other.id : this.id,
                other.customerId != null ? other.customerId : this.customerId,
                other.number != null ? other.number : this.number,
                other.type != null ? other.type : this.type,
                other.balance != null ? other.balance : this.balance,
                other.currency != null ? other.currency : this.currency,
                other.createdAt != null ? other.createdAt : this.createdAt,
                other.updatedAt != null ? other.updatedAt : LocalDateTime.now(),
                other.closedAt != null ? other.closedAt : this.closedAt,
                other.active,
                other.status != null ? other.status : this.status
        );
    }

    // Crea una nueva instancia con balance actualizado
    public Account withBalance(BigDecimal newBalance) {
        return new Account(
                id,
                customerId,
                number,
                type,
                newBalance,
                currency,
                createdAt,
                LocalDateTime.now(), // updatedAt
                closedAt,
                active,
                status
        );
    }
}
