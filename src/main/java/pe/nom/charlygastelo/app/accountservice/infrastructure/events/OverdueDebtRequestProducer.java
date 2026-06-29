package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.OverdueDebtRequestEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueDebtRequestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AvroJsonSerializer avroJsonSerializer;

    @Value("${topic.overdue-debt-request}")
    private String overdueDebtRequestTopic;

    public void send(
            String correlationId,
            OverdueDebtRequestEvent event) {

        try {
            String payload = avroJsonSerializer.serialize(event);

            log.info("Sending OverdueDebtRequestEvent. topic={}, correlationId={}, customerId={}",
                    overdueDebtRequestTopic,
                    correlationId,
                    event.getCustomerId());

            kafkaTemplate.send(overdueDebtRequestTopic, correlationId, payload)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error(
                                    "Error sending OverdueDebtRequestEvent. topic={}, correlationId={}, reason={}",
                                    overdueDebtRequestTopic,
                                    correlationId,
                                    error.getMessage(),
                                    error
                            );
                            return;
                        }

                        log.info(
                                "OverdueDebtRequestEvent sent successfully. topic={}, correlationId={}, partition={}, offset={}",
                                overdueDebtRequestTopic,
                                correlationId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    });

        } catch (Exception e) {
            log.error(
                    "Error serializing OverdueDebtRequestEvent. correlationId={}, reason={}",
                    correlationId,
                    e.getMessage(),
                    e
            );
        }
    }
}