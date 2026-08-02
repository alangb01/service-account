package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command;

import java.math.BigDecimal;

public record AccountDepositCommand(
    String transactionId,
    String customerId,
    String descripcion,
    String targetAccountId,
    BigDecimal amount
) { }
