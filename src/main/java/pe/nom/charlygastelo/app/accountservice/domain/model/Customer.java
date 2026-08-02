package pe.nom.charlygastelo.app.accountservice.domain.model;

public record Customer(
        String id,
        CustomerType customerType,
        DocumentType documentType,
        String documentNumber,
        ProfileType profileType,
        String name,
        String lastName,
        String email,
        String phone,
        Boolean active
) {
    public boolean isBusiness() {
        return CustomerType.BUSINESS == customerType;
    }

    public boolean isPersonal() {
        return CustomerType.PERSONAL == customerType;
    }

    public boolean isVip() {
        return "VIP".equals(profileType);
    }

    public boolean isPYME() {
        return "PYME".equals(profileType);
    }
}
