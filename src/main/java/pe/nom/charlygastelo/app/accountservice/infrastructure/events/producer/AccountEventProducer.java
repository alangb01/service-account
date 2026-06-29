package pe.nom.charlygastelo.app.accountservice.infrastructure.events.producer;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.events.AvroJsonSerializer;
import pe.nom.charlygastelo.app.accountservice.infrastructure.events.mapper.AccountEventMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventProducer implements AccountEventProducerPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AvroJsonSerializer serializer;
    private final AccountEventMapper mapper;

    @Value("${topic.account-created}")
    private String accountCreatedTopic;

    @Value("${topic.account-updated}")
    private String accountUpdatedTopic;

    @Value("${topic.account-closed}")
    private String accountClosedTopic;

    @Value("${topic.account-deleted}")
    private String accountDeletedTopic;

    @Override
    public Completable publishAccountCreated(Account account) {
        log.info("Publishing AccountCreatedEvent. accountId={}, customerId={}",
                account.id(), account.customerId());

        return publish(
                accountCreatedTopic,
                account.id(),
                mapper.toAccountCreatedEvent(account)
        );
    }

    @Override
    public Completable publishAccountUpdated(Account account) {
        log.info("Publishing AccountUpdatedEvent. accountId={}, customerId={}",
                account.id(), account.customerId());

        return publish(
                accountUpdatedTopic,
                account.id(),
                mapper.toAccountUpdatedEvent(account)
        );
    }

    @Override
    public Completable publishAccountClosed(Account account) {
        return publish(accountClosedTopic, account.id(), mapper.toAccountClosedEvent(account));
    }

    @Override
    public Completable publishAccountDeleted(String accountId) {
        return publish(accountDeletedTopic, accountId, mapper.toAccountDeletedEvent(accountId));
    }

    private Completable publish(String topic, String key, SpecificRecordBase event) {
        return Completable.create(emitter -> {
            try {
                log.info("[ACCOUNT-EVENT] Preparing event. topic={}, key={}, eventType={}",
                        topic, key, event.getClass().getSimpleName());

                String payload = serializer.serialize(event);

                log.debug("[ACCOUNT-EVENT] Payload serialized. key={}, payload={}", key, payload);

                kafkaTemplate.send(topic, key, payload)
                        .whenComplete((result, error) -> {
                            if (error != null) {
                                log.error("[ACCOUNT-EVENT] Error sending event. topic={}, key={}, reason={}",
                                        topic, key, error.getMessage(), error);
                                emitter.onError(error);
                                return;
                            }

                            log.info("[ACCOUNT-EVENT] Event sent. topic={}, key={}, partition={}, offset={}",
                                    topic,
                                    key,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());

                            emitter.onComplete();
                        });

            } catch (Exception e) {
                log.error("[ACCOUNT-EVENT] Unexpected error. topic={}, key={}, reason={}",
                        topic, key, e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }
}