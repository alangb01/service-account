package pe.nom.charlygastelo.app.accountservice.infrastructure.clients.dto;

public record CustomerResponse (
    String id,
    String documentType,
    String documentNumber,
    String fullName,
    String email,
    String phone,
    String status

) { }
