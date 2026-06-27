package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import java.time.Instant;
import java.util.UUID;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.TransactionEventPort;
import pe.nom.charlygastelo.app.shared.avro.dto.TransactionCompletedEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.TransactionFailedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventProducer implements TransactionEventPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AvroJsonSerializer avroJsonSerializer;

    @Value("${topic.transaction-completed}")
    private String transactionCompletedTopic;

    @Value("${topic.transaction-failed}")
    private String transactionFailedTopic;

    @Override
    public Completable publishTransactionCompleted(Transaction transaction) {
        TransactionCompletedEvent event = TransactionCompletedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("TRANSACTION_COMPLETED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setTransactionId(transaction.id())
                .setCustomerId(transaction.customerId())
                .setStatus("COMPLETED")
                .setAmount(transaction.amount().doubleValue())
                .build();

        return publish(transactionCompletedTopic, transaction.id(), event);
    }

    @Override
    public Completable publishTransactionFailed(
            Transaction transaction,
            String reason) {

        TransactionFailedEvent event = TransactionFailedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("TRANSACTION_FAILED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setTransactionId(transaction.id())
                .setCustomerId(transaction.customerId())
                .setReason(reason == null ? "" : reason)
                .build();

        return publish(transactionFailedTopic, transaction.id(), event);
    }

    private Completable publish(
            String topic,
            String key,
            SpecificRecordBase event) {

        return Completable.create(emitter -> {
            try {
                String payload = avroJsonSerializer.serialize(event);

                kafkaTemplate.send(topic, key, payload)
                        .whenComplete((result, error) -> {
                            if (error != null) {
                                emitter.onError(error);
                            } else {
                                log.info("Transaction result event published. topic={}, key={}",
                                        topic, key);
                                emitter.onComplete();
                            }
                        });

            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
}