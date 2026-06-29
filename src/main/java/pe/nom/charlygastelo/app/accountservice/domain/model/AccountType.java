package pe.nom.charlygastelo.app.accountservice.domain.model;

public enum AccountType {

    SAVINGS,

    CHECKING,

    FIXED_TERM,

    VIP_SAVINGS,

    PYME_CHECKING;

    public boolean isSavings() {
        return this == SAVINGS;
    }

    public boolean isChecking() {
        return this == CHECKING;
    }

    public boolean isFixedTerm() {
        return this == FIXED_TERM;
    }

    public boolean isVipSavings() {
        return this == VIP_SAVINGS;
    }

    public boolean isPymeChecking() {
        return this == PYME_CHECKING;
    }
}