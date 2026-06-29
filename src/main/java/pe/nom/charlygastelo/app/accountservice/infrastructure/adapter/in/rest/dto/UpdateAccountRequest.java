package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountRequest(

        @NotBlank
        String customerId,

        @NotBlank
        String customerType,

        @NotBlank
        String number,

        @NotNull
        String type,

        @NotNull
        BigDecimal balance,

        @NotBlank
        String currency,

        @NotBlank
        String updatedAt,

        boolean active,

        @NotBlank
        String status

) {
}