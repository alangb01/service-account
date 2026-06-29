package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.CustomerResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerResponseConsumer {

    private final AvroJsonDeserializer deserializer;
    private final CustomerResponseRegistry registry;

    @KafkaListener(
            topics = "${topic.customer-response}",
            groupId = "account-service")
    public void consumeCustomerResponse(String message) {
        log.debug("CustomerResponseEvent raw message received");

        try {
            CustomerResponseEvent event =
                    deserializer.deserialize(
                            message,
                            CustomerResponseEvent.class,
                            CustomerResponseEvent.getClassSchema()
                    );

            log.info(
                    "CustomerResponseEvent received. correlationId={}, customerId={}, found={}, active={}",
                    event.getCorrelationId(),
                    event.getCustomerId(),
                    event.getFound(),
                    event.getActive()
            );

            registry.complete(event);

            log.info(
                    "CustomerResponseEvent correlated successfully. correlationId={}",
                    event.getCorrelationId()
            );

        } catch (Exception e) {
            log.error(
                    "Error processing CustomerResponseEvent. reason={}",
                    e.getMessage(),
                    e
            );
        }
    }
}