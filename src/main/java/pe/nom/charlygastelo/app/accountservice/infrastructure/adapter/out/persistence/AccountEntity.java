package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    @Id
    private String id;
    private String customerId;
    private String number;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;
    private boolean active;

    // getters/setters vacíos para JPA

    public static AccountEntity fromDomain(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.id = account.id();
        entity.customerId = account.customerId();
        entity.number = account.number();
        entity.balance = account.balance();
        entity.currency = account.currency();
        entity.createdAt = account.createdAt();
        entity.active = account.active();
        return entity;
    }

    public Account toDomain() {
        return new Account(
                id, customerId, number, balance, currency, createdAt, active
        );
    }
}