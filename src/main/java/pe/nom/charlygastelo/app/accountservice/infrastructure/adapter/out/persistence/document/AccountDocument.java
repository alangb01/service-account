package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")
public class AccountDocument {

    @Id
    private String id;
    private String customerId;
    @Indexed(unique = true)
    private String number;
    private String type;

    private BigDecimal balance;
    private BigDecimal available;

    private Integer freeTransactionsLimit;   // movimientos sin comisión
    private BigDecimal minimumOpeningAmount;
    private Integer monthlyTransactionCount;
    private BigDecimal commissionAmount;


    private String currency;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;
    private boolean active;
    private AccountStatus status;
}