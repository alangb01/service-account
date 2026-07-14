package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountCreatedEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountDeletedEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountUpdatedEvent;

import java.time.Instant;
import java.util.UUID;

@Component
public class AccountManagementEventProducerMapper {

    public AccountCreatedEvent toAccountCreatedEvent(Account account) {
        return AccountCreatedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_CREATED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setAccountId(account.id())
                .setCustomerId(account.customerId())
                .setNumber(account.number())
                .setType(account.type().name())
                .setBalance(account.balance().doubleValue())
                .setAvailable(account.available().doubleValue())
                .setCurrency(account.currency())
                .setStatus(account.status().name())
                .setActive(account.isActive())
                .setCreatedAt(account.createdAt().toString())
                .build();
    }

    public AccountUpdatedEvent toAccountUpdatedEvent(Account account) {
        return null;
    }

    public AccountDeletedEvent toAccountDeletedEvent(String accountId) {
        return null;
    }
}
