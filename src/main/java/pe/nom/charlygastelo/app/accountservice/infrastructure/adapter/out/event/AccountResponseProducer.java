package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountResponseProducer {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    @Value("${topic.account-response}")
    private String accountResponseTopic;

    public void publish(String correlationId, AccountResponseEvent event) {
        try {
            log.info("[ACCOUNT-RESPONSE] Preparing to publish event. correlationId={}, found={}",
                    correlationId, event.getFound());

            log.debug("[ACCOUNT-RESPONSE] Serializing AccountResponseEvent. correlationId={}, event={}",
                    correlationId, event);


            log.debug("[ACCOUNT-RESPONSE] Payload serialized successfully. correlationId={}, payload={}",
                    correlationId, event);

            kafkaTemplate.send(accountResponseTopic, correlationId, event)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("[ACCOUNT-RESPONSE] Error sending event. correlationId={}, reason={}",
                                    correlationId, error.getMessage(), error);
                            return;
                        }

                        log.info("[ACCOUNT-RESPONSE] Event published successfully. correlationId={}, found={}, partition={}, offset={}",
                                correlationId,
                                event.getFound(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    });

        } catch (Exception e) {
            log.error("[ACCOUNT-RESPONSE] Error serializing AccountResponseEvent. correlationId={}, reason={}",
                    correlationId, e.getMessage(), e);
        }
    }
}
