package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;

public record AccountResponse (
        String id,
        String customerId,
        String number,
        String type,
        BigDecimal balance,
        String currency,
        LocalDateTime createdAt,
        boolean active,
        String status
) { }