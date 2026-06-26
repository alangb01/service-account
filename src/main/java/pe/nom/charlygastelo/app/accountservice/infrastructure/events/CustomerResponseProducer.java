package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.CustomerResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerResponseProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AvroJsonSerializer avroJsonSerializer;

    @Value("${topic.customer-response}")
    private String customerResponseTopic;

    public void publish(String correlationId, CustomerResponseEvent event) {
        try {
            String payload = avroJsonSerializer.serialize(event);

            kafkaTemplate.send(customerResponseTopic, correlationId, payload)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("Error publishing CustomerResponseEvent", error);
                        } else {
                            log.info("CustomerResponseEvent published. correlationId={}", correlationId);
                        }
                    });

        } catch (Exception e) {
            log.error("Error serializing CustomerResponseEvent", e);
        }
    }
}