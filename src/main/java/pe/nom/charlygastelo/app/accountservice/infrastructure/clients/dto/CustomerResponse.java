package pe.nom.charlygastelo.app.accountservice.infrastructure.clients.dto;

public record CustomerResponse (
    String id,
    String customerType,
    String documentType,
    String documentNumber,
    String profileType,
    String name,
    String fullName,
    String email,
    String phone,
    boolean active,
    String status
) { }
