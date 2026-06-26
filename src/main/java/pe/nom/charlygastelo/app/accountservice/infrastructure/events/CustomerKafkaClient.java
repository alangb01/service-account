package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import pe.nom.charlygastelo.app.accountservice.domain.port.CustomerEventPort;
import pe.nom.charlygastelo.app.shared.avro.dto.CustomerRequestEvent;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerKafkaClient implements CustomerEventPort {

    private final CustomerRequestProducer producer;
    private final CustomerResponseRegistry registry;

    @Override
    public Single<Customer> getById(String customerId) {

        String correlationId = UUID.randomUUID().toString();

        CustomerRequestEvent event = CustomerRequestEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("CUSTOMER_REQUEST")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setCorrelationId(correlationId)
                .setCustomerId(customerId)
                .build();

        log.info("Waiting response correlationId={}", correlationId);

        return registry.waitForResponse(correlationId)
                .doOnSubscribe(d -> {
                    log.info("Sending CustomerRequestEvent correlationId={}", correlationId);
                    producer.send(correlationId, event);
                });
    }
}