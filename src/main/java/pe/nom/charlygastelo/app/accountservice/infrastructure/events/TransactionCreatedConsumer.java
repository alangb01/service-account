package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.ProcessTransactionUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;
import pe.nom.charlygastelo.app.shared.avro.dto.TransactionCreatedEvent;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCreatedConsumer {

    private final AvroJsonDeserializer deserializer;
    private final ProcessTransactionUseCase useCase;

    @KafkaListener(topics = "${topic.transaction-created}", groupId = "account-service")
    public void consume(String message) {
        try {
            TransactionCreatedEvent event =
                    deserializer.deserialize(
                            message,
                            TransactionCreatedEvent.class,
                            TransactionCreatedEvent.getClassSchema()
                    );

            Transaction tx = new Transaction(
                    event.getTransactionId().toString(),
                    event.getCustomerId().toString(),
                    event.getSourceProductId().toString(),
                    event.getTargetProductId().toString(),
                    TransactionType.valueOf(event.getTransactionType().toString()),
                    BigDecimal.valueOf(event.getAmount()),
                    BigDecimal.valueOf(event.getCommission()),
                    event.getDescription().toString()
            );

            useCase.execute(tx)
                    .subscribe(
                            () -> log.info("Transaction processed by account-service"),
                            error -> log.error("Error processing transaction", error)
                    );

        } catch (Exception e) {
            log.error("Error consuming TransactionCreatedEvent", e);
        }
    }
}