package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.OverdueDebtResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueDebtResponseConsumer {

    private final AvroJsonDeserializer deserializer;
    private final OverdueDebtResponseRegistry registry;

    @KafkaListener(
            topics = "${topic.overdue-debt-response}",
            groupId = "account-service")
    public void consume(String message) {
        log.debug("OverdueDebtResponseEvent raw message received");

        try {
            OverdueDebtResponseEvent event =
                    deserializer.deserialize(
                            message,
                            OverdueDebtResponseEvent.class,
                            OverdueDebtResponseEvent.getClassSchema()
                    );

            log.info(
                    "OverdueDebtResponseEvent received. correlationId={}, customerId={}, hasDebt={}",
                    event.getCorrelationId(),
                    event.getCustomerId(),
                    event.getHasOverdueDebt()
            );

            registry.complete(event);

        } catch (Exception e) {
            log.error(
                    "Error processing OverdueDebtResponseEvent. reason={}",
                    e.getMessage(),
                    e
            );
        }
    }
}