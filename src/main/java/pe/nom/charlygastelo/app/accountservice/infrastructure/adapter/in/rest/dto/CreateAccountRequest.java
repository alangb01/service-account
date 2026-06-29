package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(

        @NotBlank
        String customerId,

        @NotBlank
        String number,

        @NotNull
        String type,

        @NotBlank
        String currency

) {
}