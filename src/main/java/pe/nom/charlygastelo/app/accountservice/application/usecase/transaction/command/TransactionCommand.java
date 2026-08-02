package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command;

import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;

import java.math.BigDecimal;

public record TransactionCommand(
        String transactionId,
        String customerId,
        String descripcion,
        TransactionType transactionType,
        String sourceAccountId,
        String targetAccountId,
        BigDecimal amount
) { }
