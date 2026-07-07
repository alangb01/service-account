package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountUpdateRequest(

        @NotBlank
        String customerId,

        @NotBlank
        String number,

        @NotNull
        String type,

        @NotNull
        BigDecimal balance,

        @NotBlank
        String currency,

        boolean active,

        @NotBlank
        String status

) {
}