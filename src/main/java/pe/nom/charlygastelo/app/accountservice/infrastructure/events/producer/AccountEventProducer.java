package pe.nom.charlygastelo.app.accountservice.infrastructure.events.producer;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.infrastructure.events.mapper.AccountEventMapper;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountEventProducerPort;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventProducer implements AccountEventProducerPort {
    @Value("${topic.account-created}")
    private String accountCreatedTopic;
    @Value("${topic.account-updated}")
    private String accountUpdatedTopic;
    @Value("${topic.account-closed}")
    private String accountClosedTopic;
    @Value("${topic.account-deleted}")
    private String accountDeletedTopic;
    @Value("${topic.account-balance-changed}")
    private String balanceChangedTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AccountEventMapper mapper;

    @Override
    public Completable publishAccountCreated(Account account)  {
        log.info("Publishing account created event for account ");
        var event = mapper.toAccountCreatedEvent(account);
        log.info("Publishing account created event for account {}", account.id());
        return Completable.create(emitter -> {
            try {
                log.info("Publishing account sending");
                kafkaTemplate.send(
                        accountCreatedTopic,
                        account.id(),
                        event.toString()
                ).whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Event sent to topic {} with key {}", accountCreatedTopic, account.id());
                        emitter.onComplete();
                    } else {
                        log.error("Error sending event to topic {}: {}", accountCreatedTopic, ex.getMessage());
                        emitter.onError(ex);
                    }
                });
            }
            catch (Exception e) {
                log.info("Publishing account error");
                emitter.onError(e);
            }
        });
    }

    @Override
    public Completable  publishAccountUpdated(Account account)  {
        var event = mapper.toAccountUpdatedEvent(account);

        return Completable.create(emitter -> {
            try {
                kafkaTemplate.send(
                        accountUpdatedTopic,
                        account.id(),
                        event.toString()
                );
            }
            catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    @Override
    public Completable  publishAccountClosed(Account account)  {
        var event = mapper.toAccountClosedEvent(account);

        return Completable.create(emitter -> {
            try {
                kafkaTemplate.send(
                        accountClosedTopic,
                        account.id(),
                        event.toString()
                );
            }
            catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    @Override
    public Completable publishAccountDeleted(Account account) {
        var event = mapper.toAccountClosedEvent(account);

        return Completable.create(emitter -> {
            try {
                kafkaTemplate.send(
                        accountClosedTopic,
                        account.id(),
                        event.toString()
                );
            }
            catch (Exception e) {
                emitter.onError(e);
            }
        });
    }


}
