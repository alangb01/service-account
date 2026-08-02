package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response;

import lombok.Builder;

@Builder
public record AccountHolderResponse(

        String id,

        String accountId,

        String customerId,

        String holderType
) {
}