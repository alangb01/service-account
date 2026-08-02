package pe.nom.charlygastelo.app.accountservice.domain.model;

import java.math.BigDecimal;

public record ProcessedTransaction(
    String transactionId,
    String customerId,
    BigDecimal amount,
    Account source,
    Account target
) { }
