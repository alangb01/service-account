package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;

public record CloseAccountRequest(
        String customerId,
        String closedAt,
        boolean active,
        String status
) { }
