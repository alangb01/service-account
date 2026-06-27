package pe.nom.charlygastelo.app.accountservice.domain.model;

import java.math.BigDecimal;

public record Transaction(
        String id,
        String customerId,
        String sourceProductId,
        String targetProductId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal commission,
        String description
) {
}