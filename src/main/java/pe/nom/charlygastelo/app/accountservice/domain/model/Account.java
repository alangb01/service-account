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
    public Account updateWith(Account account) {
        return null;
    }
}
