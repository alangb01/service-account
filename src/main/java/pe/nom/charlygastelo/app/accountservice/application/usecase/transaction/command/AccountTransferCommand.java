package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command;

import java.math.BigDecimal;

public record AccountTransferCommand(
        String transactionId,
        String customerId,
        String descripcion,
        String sourceAccountId,
        String targetAccountId,
        BigDecimal amount
) { }
