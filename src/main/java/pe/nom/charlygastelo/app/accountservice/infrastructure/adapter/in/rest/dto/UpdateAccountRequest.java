package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;

import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;

public record UpdateAccountRequest(
        String customerId,
        String number,
        String type,
        BigDecimal balance,
        String currency,
        String updatedAt,
        boolean active,
        String status
) { }
