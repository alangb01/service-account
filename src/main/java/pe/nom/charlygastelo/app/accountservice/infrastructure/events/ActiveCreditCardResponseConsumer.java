package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.ActiveCreditCardResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveCreditCardResponseConsumer {

    private final AvroJsonDeserializer deserializer;
    private final ActiveCreditCardResponseRegistry registry;

    @KafkaListener(
            topics = "${topic.active-credit-card-response}",
            groupId = "account-service")
    public void consume(String message) {
        log.debug("ActiveCreditCardResponseEvent raw message received");

        try {
            ActiveCreditCardResponseEvent event =
                    deserializer.deserialize(
                            message,
                            ActiveCreditCardResponseEvent.class,
                            ActiveCreditCardResponseEvent.getClassSchema()
                    );

            String correlationId = event.getCorrelationId().toString();

            log.info(
                    "ActiveCreditCardResponseEvent received. correlationId={}, customerId={}, hasActiveCreditCard={}",
                    correlationId,
                    event.getCustomerId(),
                    event.getHasActiveCreditCard()
            );

            registry.complete(
                    correlationId,
                    event.getHasActiveCreditCard()
            );

        } catch (Exception e) {
            log.error(
                    "Error processing ActiveCreditCardResponseEvent. reason={}",
                    e.getMessage(),
                    e
            );
        }
    }
}