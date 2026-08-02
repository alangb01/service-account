package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response;

import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolder;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSigner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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