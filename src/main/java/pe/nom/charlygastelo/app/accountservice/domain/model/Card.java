package pe.nom.charlygastelo.app.accountservice.domain.model;

public record Card(
        String id,
        String customerId,
        String accountId,
        String cardNumber,
        String type,
        String status
) {

    public boolean isDebit() {
        return "DEBIT".equals(type);
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}