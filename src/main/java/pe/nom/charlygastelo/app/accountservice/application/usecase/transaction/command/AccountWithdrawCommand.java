package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command;

import java.math.BigDecimal;

public record AccountWithdrawCommand(
    String transactionId,
    String customerId,
    String descripcion,
    String sourceAccountId,
    BigDecimal amount
) { }
