package pe.nom.charlygastelo.app.accountservice.domain.model;

public record ProcessedTransaction(
    Transaction transaction,
    Account source,
    Account target
) { }
