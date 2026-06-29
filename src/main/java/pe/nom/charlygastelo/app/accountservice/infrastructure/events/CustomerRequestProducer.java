package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.CustomerRequestEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerRequestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AvroJsonSerializer avroJsonSerializer;

    @Value("${topic.customer-request}")
    private String customerRequestTopic;

    public void send(String correlationId, CustomerRequestEvent event) {
        try {
            String payload = avroJsonSerializer.serialize(event);

            log.info("Sending CustomerRequestEvent. topic={}, correlationId={}, customerId={}",
                    customerRequestTopic,
                    correlationId,
                    event.getCustomerId());

            kafkaTemplate.send(customerRequestTopic, correlationId, payload)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error(
                                    "Error sending CustomerRequestEvent. topic={}, correlationId={}, reason={}",
                                    customerRequestTopic,
                                    correlationId,
                                    error.getMessage(),
                                    error
                            );
                            return;
                        }

                        log.info(
                                "CustomerRequestEvent sent successfully. topic={}, correlationId={}, partition={}, offset={}",
                                customerRequestTopic,
                                correlationId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    });

        } catch (Exception e) {
            log.error(
                    "Error serializing CustomerRequestEvent. correlationId={}, reason={}",
                    correlationId,
                    e.getMessage(),
                    e
            );
        }
    }
}