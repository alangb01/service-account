package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AccountSignerCreateRequest(

        @NotBlank
        String customerId,

        @NotBlank
        String signerRole
) {
}