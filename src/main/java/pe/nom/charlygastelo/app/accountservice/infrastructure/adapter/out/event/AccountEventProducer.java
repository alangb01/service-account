package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper.AccountEventProducerMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper.AvroJsonSerializer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AvroJsonSerializer serializer;
    private final AccountEventProducerMapper mapper;

    @Value("${topic.account-created}")
    private String accountCreatedTopic;

    @Value("${topic.account-updated}")
    private String accountUpdatedTopic;

    @Value("${topic.account-deleted}")
    private String accountDeletedTopic;

    @Value("${topic.account-response}")
    private String accountResponseTopic;


    public Completable publishAccountCreated(Account account) {
        return publish(accountCreatedTopic, account.id(), mapper.toAccountCreatedEvent(account));
    }


    public Completable publishAccountUpdated(Account account) {
        return publish(accountUpdatedTopic, account.id(), mapper.toAccountUpdatedEvent(account));
    }


    public Completable publishAccountDeleted(String accountId) {
        return publish(accountDeletedTopic, accountId, mapper.toAccountDeletedEvent(accountId));
    }

    public Completable publishAccountResponse(Account account, String correlationId) {
        return publish(accountResponseTopic, correlationId, mapper.toAccountResponseEvent(account, correlationId));
    }

    public Completable publishAccountNotFound(String accountId, String correlationId) {
        return publish(accountResponseTopic, correlationId, mapper.toAccountNotFoundEvent(accountId, correlationId));
    }

    public Completable publish(String topic, String key, SpecificRecordBase event) {
        return Completable.create(emitter -> {
            try {

                log.info("[CUSTOMER-EVENT] Preparing event. topic={}, key={}, eventType={}",
                        topic, key, event.getClass().getSimpleName());

                log.debug("[CUSTOMER-EVENT] Serializing event. key={}, event={}",
                        key, event);

                String payload = serializer.serialize(event);

                log.debug("[CUSTOMER-EVENT] Payload serialized successfully. key={}, payload={}",
                        key, payload);

                kafkaTemplate.send(topic, key, payload)
                        .whenComplete((result, error) -> {
                            if (error != null) {
                                log.error("[CUSTOMER-EVENT] Error sending event. topic={}, key={}, reason={}",
                                        topic, key, error.getMessage(), error);
                                emitter.onError(error);
                                return;
                            }

                            log.info("[CUSTOMER-EVENT] Event sent successfully. topic={}, key={}, partition={}, offset={}",
                                    topic,
                                    key,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );

                            emitter.onComplete();
                        });

            } catch (Exception e) {
                log.error("[CUSTOMER-EVENT] Unexpected error serializing or sending event. topic={}, key={}, reason={}",
                        topic, key, e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }

}
