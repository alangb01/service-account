package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.CustomerType;

@Data
@Document("accounts")
public class AccountDocument {

    @Id
    private String id;
    private String customerId;
    private CustomerType customerType;
    private String number;
    private AccountType type;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;
    private boolean active;
    private String status;
}
