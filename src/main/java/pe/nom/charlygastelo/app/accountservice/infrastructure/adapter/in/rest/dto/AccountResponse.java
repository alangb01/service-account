package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        String id,
        String customerId,
        String number,
        BigDecimal balance,
        String currency,
        LocalDateTime createdAt,
        boolean active
) {}