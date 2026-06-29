package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.CardRequestEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardRequestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AvroJsonSerializer avroJsonSerializer;

    @Value("${topic.card-request}")
    private String cardRequestTopic;

    public void send(String correlationId, CardRequestEvent event) {
        try {
            String payload = avroJsonSerializer.serialize(event);

            log.info("Sending CardRequestEvent. topic={}, correlationId={}, cardId={}",
                    cardRequestTopic,
                    correlationId,
                    event.getCardId());

            kafkaTemplate.send(cardRequestTopic, correlationId, payload)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error(
                                    "Error sending CardRequestEvent. topic={}, correlationId={}, reason={}",
                                    cardRequestTopic,
                                    correlationId,
                                    error.getMessage(),
                                    error
                            );
                            return;
                        }

                        log.info(
                                "CardRequestEvent sent successfully. topic={}, correlationId={}, partition={}, offset={}",
                                cardRequestTopic,
                                correlationId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    });

        } catch (Exception e) {
            log.error(
                    "Error serializing CardRequestEvent. correlationId={}, reason={}",
                    correlationId,
                    e.getMessage(),
                    e
            );
        }
    }
}