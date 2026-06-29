package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountDebitResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountDebitResponseProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AvroJsonSerializer avroJsonSerializer;

    @Value("${topic.account-debit-response}")
    private String accountDebitResponseTopic;

    public void publish(
            String correlationId,
            AccountDebitResponseEvent event) {

        try {
            String payload = avroJsonSerializer.serialize(event);

            log.info(
                    "Publishing AccountDebitResponseEvent. topic={}, correlationId={}, transactionId={}, success={}",
                    accountDebitResponseTopic,
                    correlationId,
                    event.getTransactionId(),
                    event.getSuccess()
            );

            kafkaTemplate.send(accountDebitResponseTopic, correlationId, payload)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error(
                                    "Error publishing AccountDebitResponseEvent. topic={}, correlationId={}, reason={}",
                                    accountDebitResponseTopic,
                                    correlationId,
                                    error.getMessage(),
                                    error
                            );
                            return;
                        }

                        log.info(
                                "AccountDebitResponseEvent published successfully. topic={}, correlationId={}, partition={}, offset={}",
                                accountDebitResponseTopic,
                                correlationId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    });

        } catch (Exception e) {
            log.error(
                    "Error serializing AccountDebitResponseEvent. correlationId={}, reason={}",
                    correlationId,
                    e.getMessage(),
                    e
            );
        }
    }
}