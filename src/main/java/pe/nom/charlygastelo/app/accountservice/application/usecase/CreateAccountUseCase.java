package pe.nom.charlygastelo.app.accountservice.application.usecase;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.clients.CustomerClient;
import pe.nom.charlygastelo.app.accountservice.infrastructure.events.AccountEventProducer;
import pe.nom.charlygastelo.app.customerservice.infrastructure.avro.events.AccountCreatedEvent;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;

public class CreateAccountUseCase {

    private final AccountServicePort service;
    private final AccountEventProducer eventProducer;
    private final CustomerClient customerClient;


    public CreateAccountUseCase(AccountServicePort service, CustomerClient customerClient, AccountEventProducer eventProducer) {
        this.service = service;
        this.customerClient = customerClient;
        this.eventProducer = eventProducer;
    }

    public Mono<Account> execute(Account account) {
        return service.create(account).doOnSuccess(saved -> {
            AccountCreatedEvent event = new AccountCreatedEvent(
                saved.id(),
                saved.customerId(),
                saved.type().toString(),
                saved.balance().doubleValue(),
                saved.createdAt().atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
            );
            eventProducer.publishAAccountCreated(event);
        });
    }
}