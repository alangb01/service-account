package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;

import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;

public record CreateAccountRequest (
        String customerId,
        String number,
        String type,
        String currency
) { }