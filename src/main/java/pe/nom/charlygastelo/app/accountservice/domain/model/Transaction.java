package pe.nom.charlygastelo.app.accountservice.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(
        String id,
        String customerId,
        String sourceProductType,
        String targetProductType,
        String sourceProductId,
        String targetProductId,
        String type,
        BigDecimal amount,
        BigDecimal commission,
        String description,
        Instant timestamp
) {
}