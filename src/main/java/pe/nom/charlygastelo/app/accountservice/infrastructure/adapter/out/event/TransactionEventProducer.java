package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper.TransactionEventOutMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final TransactionEventOutMapper transactionMapper;

//    @Value("${topic.transaction-completed}")
//    private String transactionCompletedTopic;

    @Value("${topic.transaction-failed}")
    private String transactionFailedTopic;

//    public Completable publishTransactionCompleted(Transaction transaction) {
//        return publish(transactionCompletedTopic, transaction.id(), transactionMapper.toTransactionCompletedEvent(transaction));
//    }

    public Completable publishTransactionFailed(Transaction transaction, String reason) {
        return publish(transactionFailedTopic, transaction.id(), transactionMapper.toTransactionFailedEvent(transaction, reason));
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
                                log.error(
                                        "Error publishing transaction event. topic={}, key={}, eventClass={}, reason={}",
                                        topic,
                                        key,
                                        event.getClass().getSimpleName(),
                                        error.getMessage(),
                                        error
                                );
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
                log.error(
                        "Error serializing transaction event. topic={}, key={}, eventClass={}, reason={}",
                        topic,
                        key,
                        event.getClass().getSimpleName(),
                        e.getMessage(),
                        e
                );
                emitter.onError(e);
            }
        });
    }

}
