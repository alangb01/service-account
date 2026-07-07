package pe.nom.charlygastelo.app.accountservice.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;

public record AccountSigner(
        String id,
        String accountId,
        String customerId,
        SignerRole signerRole,
        Instant validFrom,
        Instant validTo,
        Instant createdAt,
        Instant updatedAt,
        AccountSignerStatus status
        ) {

}
