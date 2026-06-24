package pe.nom.charlygastelo.app.accountservice.infrastructure.events.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.customerservice.infrastructure.avro.events.AccountCreatedEvent;
import pe.nom.charlygastelo.app.customerservice.infrastructure.avro.events.AccountUpdatedEvent;
import pe.nom.charlygastelo.app.customerservice.infrastructure.avro.events.AccountClosedEvent;

import java.time.ZoneOffset;

@Component
public class AccountEventMapper {

    public AccountCreatedEvent toAccountCreatedEvent(Account account) {
        return new AccountCreatedEvent(
                account.id(),
                account.customerId(),
                account.type().name(),
                account.balance().doubleValue(),
                account.createdAt().toInstant(ZoneOffset.UTC).toEpochMilli()
        );
    }

    public Object toAccountClosedEvent(Account account) {
        return new AccountClosedEvent(
            account.id(),
            account.closedAt().toInstant(ZoneOffset.UTC).toEpochMilli()
        );
    }

    public Object toAccountUpdatedEvent(Account account) {
        return new AccountCreatedEvent(
                account.id(),
                account.customerId(),
                account.type().name(),
                account.balance().doubleValue(),
                account.closedAt().toInstant(ZoneOffset.UTC).toEpochMilli()
        );
    }
}
