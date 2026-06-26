package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.CustomerResponseEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerResponseConsumer {

    private final ObjectMapper objectMapper;
    private final CustomerResponseRegistry registry;

    @KafkaListener(topics = "${topic.customer-response}", groupId = "account-service")
    public void consumeCustomerResponse(String message) {
        try {
            CustomerResponseEvent event =
                    objectMapper.readValue(message, CustomerResponseEvent.class);

            registry.complete(event);

        }
        catch (Exception e) {
            log.error("Error processing CustomerResponseEvent", e);
        }
    }
}