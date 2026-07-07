package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.client.dto;

public record CustomerResponse (
    String id,
    String customerType,
    String documentType,
    String documentNumber,
    String profileType,
    String name,
    String lastName,
    String email,
    String phone,
    Boolean active,
    String status
) { }
