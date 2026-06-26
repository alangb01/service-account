package pe.nom.charlygastelo.app.accountservice.infrastructure.events;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.CustomerRequestEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerRequestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AvroJsonSerializer avroJsonSerializer;

    @Value("${topic.customer-request}")
    private String customerRequestTopic;

    public void send(String correlationId, CustomerRequestEvent event) {
        try {
            String payload = avroJsonSerializer.serialize(event);

            kafkaTemplate.send(customerRequestTopic, correlationId, payload)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("Error sending CustomerRequestEvent", error);
                        }
                    });

        }
        catch (Exception e) {
            log.error("Error serializing CustomerRequestEvent", e);
        }
    }
}