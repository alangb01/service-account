package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolderStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.HolderType;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "account_holders")
public class AccountHolderDocument {
    @Id
    private String id;

    private String accountId;
    private String customerId;
    private HolderType holderType;
    private Instant validFrom;
    private Instant validTo;
    private Instant createdAt;
    private Instant updatedAt;
    private AccountHolderStatus status;
}
