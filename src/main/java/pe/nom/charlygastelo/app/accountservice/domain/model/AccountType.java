package pe.nom.charlygastelo.app.accountservice.domain.model;

public enum AccountType {
    SAVINGS,
    CHECKING,
    FIXED_TERM;

    public boolean isSavings() {
        return this == SAVINGS;
    }

    public boolean isFixedTerm() {
        return this == FIXED_TERM;
    }
}
