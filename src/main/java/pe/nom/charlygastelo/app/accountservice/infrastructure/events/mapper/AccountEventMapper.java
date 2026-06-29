package pe.nom.charlygastelo.app.accountservice.infrastructure.events.mapper;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountStatus;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountClosedEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountCreatedEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountDeletedEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountResponseEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountUpdatedEvent;

@Component
public class AccountEventMapper {

    public AccountCreatedEvent toAccountCreatedEvent(Account account) {
        return AccountCreatedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_CREATED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setAccountId(value(account.id()))
                .setCustomerId(value(account.customerId()))
                .setNumber(value(account.number()))
                .setType(account.type().name())
                .setBalance(account.balance().doubleValue())
                .setStatus(status(account))
                .build();
    }

    public AccountUpdatedEvent toAccountUpdatedEvent(Account account) {
        return AccountUpdatedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_UPDATED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setAccountId(value(account.id()))
                .setCustomerId(value(account.customerId()))
                .setNumber(value(account.number()))
                .setType(account.type().name())
                .setBalance(account.balance().doubleValue())
                .setStatus(status(account))
                .build();
    }

    public AccountClosedEvent toAccountClosedEvent(Account account) {
        return AccountClosedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_CLOSED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setAccountId(value(account.id()))
                .setCustomerId(value(account.customerId()))
                .setStatus(AccountStatus.CLOSED.name())
                .build();
    }

    public AccountDeletedEvent toAccountDeletedEvent(String accountId) {
        return AccountDeletedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_DELETED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setAccountId(value(accountId))
                .build();
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
                .setCorrelationId(value(correlationId))
                .setFound(true)
                .setAccountId(value(account.id()))
                .setCustomerId(value(account.customerId()))
                .setNumber(value(account.number()))
                .setType(account.type().name())
                .setBalance(account.balance().doubleValue())
                .setActive(account.isActive())
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
                .setCorrelationId(value(correlationId))
                .setFound(false)
                .setAccountId(value(accountId))
                .setCustomerId("")
                .setNumber("")
                .setType("")
                .setBalance(0.0)
                .setActive(false)
                .build();
    }

    private String status(Account account) {
        return account.status() == null
                ? AccountStatus.ACTIVE.name()
                : account.status().name();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}