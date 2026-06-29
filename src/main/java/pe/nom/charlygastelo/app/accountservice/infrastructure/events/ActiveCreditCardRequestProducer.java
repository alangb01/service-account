package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.ActiveCreditCardRequestEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveCreditCardRequestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AvroJsonSerializer avroJsonSerializer;

    @Value("${topic.active-credit-card-request}")
    private String activeCreditCardRequestTopic;

    public void send(
            String correlationId,
            ActiveCreditCardRequestEvent event) {

        try {
            String payload = avroJsonSerializer.serialize(event);

            log.info(
                    "Sending ActiveCreditCardRequestEvent. topic={}, correlationId={}, customerId={}",
                    activeCreditCardRequestTopic,
                    correlationId,
                    event.getCustomerId()
            );

            kafkaTemplate.send(activeCreditCardRequestTopic, correlationId, payload)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error(
                                    "Error sending ActiveCreditCardRequestEvent. topic={}, correlationId={}, reason={}",
                                    activeCreditCardRequestTopic,
                                    correlationId,
                                    error.getMessage(),
                                    error
                            );
                            return;
                        }

                        log.info(
                                "ActiveCreditCardRequestEvent sent successfully. topic={}, correlationId={}, partition={}, offset={}",
                                activeCreditCardRequestTopic,
                                correlationId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    });

        } catch (Exception e) {
            log.error(
                    "Error serializing ActiveCreditCardRequestEvent. correlationId={}, reason={}",
                    correlationId,
                    e.getMessage(),
                    e
            );
        }
    }
}