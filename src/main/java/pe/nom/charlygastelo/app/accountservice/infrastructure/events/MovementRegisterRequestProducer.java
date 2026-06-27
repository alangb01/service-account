package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.MovementRegisterRequestEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovementRegisterRequestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AvroJsonSerializer avroJsonSerializer;

    @Value("${topic.movement-register-request}")
    private String movementRegisterRequestTopic;

    public void send(String correlationId, MovementRegisterRequestEvent event) {
        try {
            String payload = avroJsonSerializer.serialize(event);

            kafkaTemplate.send(movementRegisterRequestTopic, correlationId, payload)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("Error sending MovementRegisterRequestEvent", error);
                        } else {
                            log.info("MovementRegisterRequestEvent published. correlationId={}", correlationId);
                        }
                    });

        } catch (Exception e) {
            log.error("Error serializing MovementRegisterRequestEvent", e);
        }
    }
}