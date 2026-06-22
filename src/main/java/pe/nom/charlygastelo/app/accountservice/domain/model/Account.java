package pe.nom.charlygastelo.app.accountservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record Account(
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
