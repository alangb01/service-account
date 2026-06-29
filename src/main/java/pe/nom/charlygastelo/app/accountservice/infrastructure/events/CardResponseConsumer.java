package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.CardResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardResponseConsumer {

    private final AvroJsonDeserializer deserializer;
    private final CardResponseRegistry registry;

    @KafkaListener(
            topics = "${topic.card-response}",
            groupId = "account-service")
    public void consume(String message) {
        log.debug("CardResponseEvent raw message received");

        try {
            CardResponseEvent event =
                    deserializer.deserialize(
                            message,
                            CardResponseEvent.class,
                            CardResponseEvent.getClassSchema()
                    );

            log.info(
                    "CardResponseEvent received. correlationId={}, cardId={}, found={}, status={}",
                    event.getCorrelationId(),
                    event.getCardId(),
                    event.getFound(),
                    event.getStatus()
            );

            registry.complete(event);

        } catch (Exception e) {
            log.error(
                    "Error processing CardResponseEvent. reason={}",
                    e.getMessage(),
                    e
            );
        }
    }
}