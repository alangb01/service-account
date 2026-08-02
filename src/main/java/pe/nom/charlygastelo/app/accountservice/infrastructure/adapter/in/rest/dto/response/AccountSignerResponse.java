package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response;

import lombok.Builder;

@Builder
public record AccountSignerResponse(

        String id,

        String accountId,

        String customerId,

        String signerRole
) {
}