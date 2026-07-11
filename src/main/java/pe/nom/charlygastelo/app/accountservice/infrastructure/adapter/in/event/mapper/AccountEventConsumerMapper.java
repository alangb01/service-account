package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountResponseEvent;

import java.time.Instant;
import java.util.UUID;

@Component
public class AccountEventConsumerMapper {

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
                .setType(value(account.type().name()))
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
                .setCorrelationId(value(correlationId))
                .setFound(false)
                .setAccountId(value(accountId))
                .build();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}