package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AccountHolderCreateRequest(

        @NotBlank
        String customerId,

        @NotBlank
        String holderType
) {
}