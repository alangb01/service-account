package pe.nom.charlygastelo.app.accountservice.domain.model;

public record Customer(
        String id,
        String customerType,
        String documentType,
        String documentNumber,
        String profileType,
        String name,
        String lastName,
        String email,
        String phone,
        boolean active
) {
    public boolean isBusiness() {
        return "BUSINESS".equals(customerType);
    }

    public boolean isPersonal() {
        return "PERSONAL".equals(customerType);
    }

    public boolean isVip() {
        return "VIP".equals(profileType);
    }

    public boolean isPYME() {
        return "PYME".equals(profileType);
    }
}
