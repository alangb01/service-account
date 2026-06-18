package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document("accounts")
public class AccountDocument {

    @Id
    private String id;
    private String customerId;
    private String number;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;
    private boolean active;
}
