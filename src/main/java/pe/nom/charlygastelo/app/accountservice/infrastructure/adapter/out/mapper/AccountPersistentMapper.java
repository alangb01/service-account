package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.AccountDocument;

@Component
public class AccountPersistentMapper {
    public AccountDocument toDocument(Account a) {
        AccountDocument d = new AccountDocument();
        d.setId(a.id());
        d.setCustomerId(a.customerId());
        d.setNumber(a.number());
        d.setType(a.type().toString());
        d.setBalance(a.balance());
        d.setCurrency(a.currency());
        d.setCreatedAt(a.createdAt());
        d.setActive(a.active());
        d.setStatus(a.status());
        return d;
    }

    public Account toDomain(AccountDocument d) {
        return new Account(
                d.getId(),
                d.getCustomerId(),
                d.getNumber(),
                AccountType.valueOf(d.getType()),
                d.getBalance(),
                d.getCurrency(),
                d.getCreatedAt(),
                d.getUpdatedAt(),
                d.getClosedAt(),
                d.isActive(),
                d.getStatus()
        );
    }
}
