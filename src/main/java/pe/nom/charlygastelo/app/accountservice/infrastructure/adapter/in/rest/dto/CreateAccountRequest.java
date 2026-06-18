package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;

public record CreateAccountRequest(
        String customerId,
        String number,
        BigDecimal initialBalance,
        String currency
) {}