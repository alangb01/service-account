package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;

import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.CustomerType;

public record UpdateAccountRequest(
        String customerId,
        CustomerType customerType,
        String number,
        AccountType type,
        BigDecimal balance,
        String currency,
        boolean active,
        String status
) { }
