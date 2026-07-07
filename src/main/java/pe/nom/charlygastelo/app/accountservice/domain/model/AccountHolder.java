package pe.nom.charlygastelo.app.accountservice.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;

public record AccountHolder(
    String id,
    String accountId,
    String customerId,
    HolderType holderType,
    Instant validFrom,
    Instant validTo,
    Instant createdAt,
    Instant updatedAt,
    AccountHolderStatus status
    ) {

    public AccountHolder newAccount(String accountId) {
        return new AccountHolder(
                this.id,
                accountId,
                this.customerId,
                this.holderType,
                this.validFrom,
                this.validTo,
                this.createdAt,
                null,
                AccountHolderStatus.ACTIVE

        );
    }
}
