package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.event.AccountManagementEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper.AccountManagementEventProducerMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountManagementEventProducer implements AccountManagementEventProducerPort {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final AccountManagementEventProducerMapper mapper;

    @Value("${topic.account-created}")
    private String accountCreatedTopic;

    @Value("${topic.account-updated}")
    private String accountUpdatedTopic;

    @Value("${topic.account-deleted}")
    private String accountDeletedTopic;

    @Value("${topic.account-closed}")
    private String accountClosedTopic;

    @Override
    public Completable publishAccountCreated(Account account) {
        log.info("Publishing account created. accountId={}", account.id());
        return publish(accountCreatedTopic, account.id(), mapper.toAccountCreatedEvent(account));
    }

    @Override
    public Completable publishAccountUpdated(Account account) {
        log.info("Publishing account updated. accountId={}", account.id());
        return publish(accountUpdatedTopic, account.id(), mapper.toAccountUpdatedEvent(account));
    }

    @Override
    public Completable publishAccountClosed(Account account) {
        log.info("Publishing account closed. accountId={}", account.id());
        return publish(accountClosedTopic, account.id(), mapper.toAccountDeletedEvent(account.id()));
    }

    @Override
    public Completable publishAccountDeleted(String accountId) {
        log.info("Publishing account deleted. accountId={}", accountId);
        return publish(accountDeletedTopic, accountId, mapper.toAccountDeletedEvent(accountId));
    }



    public Completable publish(String topic, String key, SpecificRecordBase event) {
        return Completable.create(emitter -> {
            try {

                log.info("[ACCOUNT-EVENT] Preparing event. topic={}, key={}, eventType={}",
                        topic, key, event.getClass().getSimpleName());

                log.debug("[ACCOUNT-EVENT] Serializing event. key={}, event={}",
                        key, event);


                log.debug("[ACCOUNT-EVENT] Payload serialized successfully. key={}, payload={}",
                        key, event);

                kafkaTemplate.send(topic, key, event)
                        .whenComplete((result, error) -> {
                            if (error != null) {
                                log.error("[ACCOUNT-EVENT] Error sending event. topic={}, key={}, reason={}",
                                        topic, key, error.getMessage(), error);
                                emitter.onError(error);
                                return;
                            }

                            log.info("[ACCOUNT-EVENT] Event sent successfully. topic={}, key={}, partition={}, offset={}",
                                    topic,
                                    key,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );

                            emitter.onComplete();
                        });

            } catch (Exception e) {
                log.error("[ACCOUNT-EVENT] Unexpected error serializing or sending event. topic={}, key={}, reason={}",
                        topic, key, e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }
}
