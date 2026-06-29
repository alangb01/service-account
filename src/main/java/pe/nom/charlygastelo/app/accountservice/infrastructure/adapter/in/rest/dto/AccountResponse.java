package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(

        String id,

        String customerId,

        String number,

        String type,

        BigDecimal balance,

        String currency,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        LocalDateTime closedAt,

        boolean active,

        String status

) {
}