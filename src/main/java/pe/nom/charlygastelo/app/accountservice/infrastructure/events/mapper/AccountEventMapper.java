package pe.nom.charlygastelo.app.accountservice.infrastructure.events.mapper;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Component;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountCreatedEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountClosedEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountResponseEvent;



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

    public AccountResponseEvent toAccountResponseEvent(
            Account account,
            String correlationId) {

        return AccountResponseEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_RESPONSE")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setCorrelationId(correlationId)
                .setFound(true)
                .setAccountId(account.id())
                .setCustomerId(account.customerId())
                .setNumber(account.number())
                .setType(account.type().name())
                .setBalance(account.balance().doubleValue())
                .setActive(account.active())
                .build();
    }

    public AccountResponseEvent toAccountNotFoundEvent(
            String accountId,
            String correlationId) {

        return AccountResponseEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_RESPONSE")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setCorrelationId(correlationId)
                .setFound(false)
                .setAccountId(accountId)
                .setCustomerId("")
                .setNumber("")
                .setType("")
                .setBalance(0.0)
                .setActive(false)
                .build();
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
