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

    @Value("${topic.transaction-failed}")
    private String transactionFailedTopic;

    public Completable publishTransactionFailed(String transactionId, String customerId, String reason) {
        return publish(
                transactionFailedTopic,
                transactionId,
                transactionMapper.toTransactionFailedEvent(transactionId, customerId, reason)
        );
    }

    public Completable publish(String topic, String key, SpecificRecordBase event) {
        return Completable.fromFuture(
            kafkaTemplate.send(topic, key, event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("[ACCOUNT-EVENT] Error sending event. topic={}, key={}, reason={}",
                            topic, key, error.getMessage(), error);
                    } else {
                        log.info("[ACCOUNT-EVENT] Event sent successfully. topic={}, key={}, partition={}, offset={}",
                            topic, key,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    }
                })
            );
    }
}
