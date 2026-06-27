package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountResponseProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AvroJsonSerializer avroJsonSerializer;

    @Value("${topic.account-response}")
    private String customerResponseTopic;

    public void publish(String correlationId, AccountResponseEvent event) {
        try {
            String payload = avroJsonSerializer.serialize(event);

            log.info("[AccountResponseProducer] Preparing to publish event. topic={}, correlationId={}, payload={}",
                    customerResponseTopic, correlationId, payload);

            kafkaTemplate.send(customerResponseTopic, correlationId, payload)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("[AccountResponseProducer] Error publishing event. topic={}, correlationId={}, error={}",
                                    customerResponseTopic, correlationId, error.getMessage(), error);
                        } else {
                            log.info("[AccountResponseProducer] Event published successfully. topic={}, correlationId={}, partition={}, offset={}",
                                    customerResponseTopic,
                                    correlationId,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });

        } catch (Exception e) {
            log.error("[AccountResponseProducer] Error serializing AccountResponseEvent. correlationId={}, error={}",
                    correlationId, e.getMessage(), e);
        }
    }
}
