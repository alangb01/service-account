package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(

        String id,

        String customerId,

        String number,

        String type,

        BigDecimal balance,

        String currency,

        Instant createdAt,

        Instant updatedAt,

        Instant closedAt,

        boolean active,

        String status

) {
}