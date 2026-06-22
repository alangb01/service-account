package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.CustomerType;

public record AccountResponse (
        String id,
        String customerId,
        CustomerType customerType,
        String number,
        AccountType type,
        BigDecimal balance,
        String currency,
        LocalDateTime createdAt,
        boolean active,
        String status
) { }